package com.hdownloader.core.download.manager

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hdownloader.core.common.DispatchersProvider
import com.hdownloader.core.database.HDownloaderDatabase
import com.hdownloader.core.download.RangeServerDispatcher
import com.hdownloader.core.download.engine.DownloadCancellation
import com.hdownloader.core.download.engine.DownloadEngine
import com.hdownloader.core.download.engine.HttpDownloader
import com.hdownloader.core.download.model.DownloadStatus
import com.hdownloader.core.download.notification.DownloadNotifier
import com.hdownloader.core.download.repository.RoomDownloadRepository
import com.hdownloader.core.download.storage.FileDownloadStorage
import com.hdownloader.core.history.repository.RoomDownloadHistoryRepository
import com.hdownloader.core.media.repository.RoomMediaRepository
import com.hdownloader.core.settings.AppSettingsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DownloadManagerIntegrationTest {

    private lateinit var server: MockWebServer
    private lateinit var database: HDownloaderDatabase
    private lateinit var storage: FileDownloadStorage
    private lateinit var manager: DownloadManager
    private lateinit var repository: RoomDownloadRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HDownloaderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storage = FileDownloadStorage(context)
        repository = RoomDownloadRepository(database.downloadDao())

        val engine = DownloadEngine(
            downloadDao = database.downloadDao(),
            partDao = database.downloadPartDao(),
            downloader = HttpDownloader(OkHttpClient()),
            storage = storage,
            historyRepository = RoomDownloadHistoryRepository(database.downloadHistoryDao()),
            mediaRepository = RoomMediaRepository(database.mediaDao()),
            settingsRepository = AppSettingsRepository(context),
            cancellation = DownloadCancellation(),
            dispatchers = DispatchersProvider(),
        )
        manager = DownloadManager(
            downloadDao = database.downloadDao(),
            engine = engine,
            cancellation = DownloadCancellation(),
            storage = storage,
            notifier = DownloadNotifier(context),
            settingsRepository = AppSettingsRepository(context),
            dispatchers = DispatchersProvider(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    @Test
    fun queuedDownloadCompletesThroughManager() = runBlocking {
        val payload = ByteArray(48_000) { (it % 251).toByte() }
        server.dispatcher = RangeServerDispatcher(payload)

        val id = repository.addDownload(
            url = server.url("/clip.mp4").toString(),
            fileName = "clip.mp4",
            startImmediately = true,
        )

        drainUntilTerminal(id)

        val state = database.downloadDao().getById(id)
        assertEquals("error: ${state?.errorMessage}", DownloadStatus.COMPLETED, state?.status)
        assertTrue(storage.targetFile("clip.mp4").exists())
        assertEquals(payload.toList(), storage.targetFile("clip.mp4").readBytes().toList())
    }

    @Test
    fun pausedDownloadIsOnlyStartedAfterResume() = runBlocking {
        val payload = ByteArray(8_000) { 9 }
        server.dispatcher = RangeServerDispatcher(payload)

        val id = repository.addDownload(
            url = server.url("/paused.bin").toString(),
            fileName = "paused.bin",
            startImmediately = false,
        )

        manager.processPending()
        delay(300)
        assertEquals(DownloadStatus.PAUSED, database.downloadDao().getById(id)?.status)
        assertFalse(storage.targetFile("paused.bin").exists())

        repository.resume(id)
        drainUntilTerminal(id)

        assertEquals(DownloadStatus.COMPLETED, database.downloadDao().getById(id)?.status)
        assertTrue(storage.targetFile("paused.bin").exists())
    }

    @Test
    fun cancelledQueuedDownloadLeavesNoFile() = runBlocking {
        server.dispatcher = RangeServerDispatcher(ByteArray(8_000) { 5 })

        val id = repository.addDownload(
            url = server.url("/cancelled.bin").toString(),
            fileName = "cancelled.bin",
            startImmediately = false,
        )
        repository.cancel(id)

        manager.processPending()
        delay(300)

        assertEquals(DownloadStatus.CANCELLED, database.downloadDao().getById(id)?.status)
        assertFalse(storage.targetFile("cancelled.bin").exists())
    }

    @Test
    fun autoRetryRecoversFromTransientFailure() = runBlocking {
        val payload = ByteArray(32_000) { (it % 251).toByte() }
        server.dispatcher = FlakyRangeDispatcher(payload, failFirst = 1)

        val id = repository.addDownload(
            url = server.url("/flaky.bin").toString(),
            fileName = "flaky.bin",
            startImmediately = true,
        )

        drainUntilTerminal(id)

        val state = database.downloadDao().getById(id)
        assertEquals(DownloadStatus.COMPLETED, state?.status)
        assertTrue("expected at least one retry", (state?.retryCount ?: 0) >= 1)
        assertTrue(storage.targetFile("flaky.bin").exists())
    }

    private suspend fun drainUntilTerminal(id: Long) {
        withTimeout(15_000L) {
            while (true) {
                val status = database.downloadDao().getById(id)?.status
                if (status == DownloadStatus.COMPLETED ||
                    status == DownloadStatus.FAILED ||
                    status == DownloadStatus.CANCELLED
                ) {
                    return@withTimeout
                }
                manager.processPending()
                delay(100)
            }
        }
    }

    private class FlakyRangeDispatcher(
        private val payload: ByteArray,
        private val failFirst: Int,
    ) : Dispatcher() {
        private var requestCount = 0
        private val delegate = RangeServerDispatcher(payload)

        override fun dispatch(request: RecordedRequest): MockResponse {
            requestCount++
            if (requestCount <= failFirst) {
                return MockResponse().setResponseCode(503).setBody("unavailable")
            }
            return delegate.dispatch(request)
        }
    }
}
