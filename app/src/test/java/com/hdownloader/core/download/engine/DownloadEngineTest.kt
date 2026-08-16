package com.hdownloader.core.download.engine

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hdownloader.core.common.DispatchersProvider
import com.hdownloader.core.database.HDownloaderDatabase
import com.hdownloader.core.database.entity.DownloadEntity
import com.hdownloader.core.database.entity.DownloadPartEntity
import com.hdownloader.core.download.FailingServerDispatcher
import com.hdownloader.core.download.PlainServerDispatcher
import com.hdownloader.core.download.RangeServerDispatcher
import com.hdownloader.core.download.model.DownloadStatus
import com.hdownloader.core.download.storage.FileDownloadStorage
import com.hdownloader.core.history.repository.RoomDownloadHistoryRepository
import com.hdownloader.core.media.model.MediaCategory
import com.hdownloader.core.media.repository.RoomMediaRepository
import com.hdownloader.core.settings.AppSettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DownloadEngineTest {

    private lateinit var server: MockWebServer
    private lateinit var database: HDownloaderDatabase
    private lateinit var storage: FileDownloadStorage
    private lateinit var engine: DownloadEngine
    private lateinit var cancellation: DownloadCancellation
    private lateinit var settings: AppSettingsRepository

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HDownloaderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storage = FileDownloadStorage(context)
        cancellation = DownloadCancellation()
        settings = AppSettingsRepository(context)
        engine = DownloadEngine(
            downloadDao = database.downloadDao(),
            partDao = database.downloadPartDao(),
            downloader = HttpDownloader(OkHttpClient()),
            storage = storage,
            historyRepository = RoomDownloadHistoryRepository(database.downloadHistoryDao()),
            mediaRepository = RoomMediaRepository(database.mediaDao()),
            settingsRepository = settings,
            cancellation = cancellation,
            dispatchers = DispatchersProvider(),
        )
    }

    @After
    fun tearDown() {
        server.shutdown()
        database.close()
    }

    private suspend fun insertQueued(url: String, fileName: String): Long =
        database.downloadDao().upsert(
            DownloadEntity(url = url, fileName = fileName, status = DownloadStatus.QUEUED),
        )

    @Test
    fun segmentedDownloadMergesPartsInOrder() = runBlocking {
        val payload = ByteArray(100_000) { (it % 251).toByte() }
        val recorder = RequestRecorder(payload)
        server.dispatcher = recorder

        val id = insertQueued(server.url("/video.mp4").toString(), "video.mp4")
        engine.execute(id)

        val state = database.downloadDao().getById(id)
        println("SEGMENTED ranges=${recorder.ranges} parts=${database.downloadPartDao().getParts(id).map { it.currentByte }} err=${state?.errorMessage}")
        assertEquals("error: ${state?.errorMessage}", DownloadStatus.COMPLETED, state?.status)
        assertEquals(100_000L, state?.totalBytes)
        assertTrue("expected multiple connections, got ${state?.connectionCount}", (state?.connectionCount ?: 0) > 1)

        val file = storage.targetFile("video.mp4")
        assertTrue(file.exists())
        assertEquals(payload.toList(), file.readBytes().toList())
        assertTrue(storage.partFile(id, 0).exists().not())

        val media = database.mediaDao().observeAll().first()
        assertEquals(1, media.size)
        assertEquals("video.mp4", media.first().fileName)
        assertEquals(MediaCategory.VIDEO, media.first().category)

        val history = database.downloadHistoryDao().observeAll().first()
        assertEquals(1, history.size)
    }

    @Test
    fun downloadsFromServerWithoutRangeSupport() = runBlocking {
        val payload = ByteArray(24_000) { 7 }
        server.dispatcher = PlainServerDispatcher(payload)

        val id = insertQueued(server.url("/plain.bin").toString(), "plain.bin")
        engine.execute(id)

        val state = database.downloadDao().getById(id)
        assertEquals("error: ${state?.errorMessage}", DownloadStatus.COMPLETED, state?.status)
        assertEquals(1, state?.connectionCount)
        assertEquals(payload.toList(), storage.targetFile("plain.bin").readBytes().toList())
    }

    @Test
    fun resumesFromPersistedPartOffsets() = runBlocking {
        val payload = ByteArray(64_000) { (it % 251).toByte() }
        val recorder = RequestRecorder(payload)
        server.dispatcher = recorder

        val id = insertQueued(server.url("/resume.bin").toString(), "resume.bin")
        database.downloadDao().updateMeta(
            id = id,
            finalUrl = server.url("/resume.bin").toString(),
            totalBytes = 64_000L,
            supportsRange = true,
            connectionCount = 2,
            filePath = storage.targetFile("resume.bin").absolutePath,
            mimeType = "application/octet-stream",
        )
        database.downloadDao().updateStatus(id, DownloadStatus.QUEUED, false)
        database.downloadDao().updateStartedAt(id, System.currentTimeMillis())
        database.downloadPartDao().upsertAll(
            listOf(
                DownloadPartEntity(downloadId = id, partIndex = 0, startByte = 0L, endByte = 31_999L, currentByte = 10_000L),
                DownloadPartEntity(downloadId = id, partIndex = 1, startByte = 32_000L, endByte = 63_999L, currentByte = 42_000L),
            ),
        )
        storage.partFile(id, 0).writeBytes(payload.copyOfRange(0, 10_000))
        storage.partFile(id, 1).writeBytes(payload.copyOfRange(32_000, 42_000))

        engine.execute(id)

        val state = database.downloadDao().getById(id)
        assertEquals(DownloadStatus.COMPLETED, state?.status)
        assertEquals(payload.toList(), storage.targetFile("resume.bin").readBytes().toList())
        assertTrue(
            "server should have received range requests from persisted offsets",
            recorder.ranges.contains("bytes=10000-31999") &&
                recorder.ranges.contains("bytes=42000-63999"),
        )
    }

    @Test
    fun failsWhenServerReturnsError() = runBlocking {
        server.dispatcher = FailingServerDispatcher()

        val id = insertQueued(server.url("/fail.bin").toString(), "fail.bin")
        engine.execute(id)

        val state = database.downloadDao().getById(id)
        assertEquals(DownloadStatus.FAILED, state?.status)
        assertNotNull(state?.errorMessage)
    }

    @Test
    fun preCancelledDownloadWritesNoFile() = runBlocking {
        val payload = ByteArray(16_000) { 3 }
        server.dispatcher = RangeServerDispatcher(payload)

        val id = insertQueued(server.url("/cancel.bin").toString(), "cancel.bin")
        cancellation.register(id)
        cancellation.requestCancellation(id)

        var threw = false
        try {
            engine.execute(id)
        } catch (e: CancellationException) {
            threw = true
        }
        val state = database.downloadDao().getById(id)
        assertTrue("engine should abort with CancellationException (status=${state?.status}, error=${state?.errorMessage})", threw)
        assertFalse(storage.targetFile("cancel.bin").exists())
    }

    private class RequestRecorder(private val payload: ByteArray) : Dispatcher() {
        val ranges = ArrayList<String>()

        override fun dispatch(request: RecordedRequest): MockResponse {
            request.getHeader("Range")?.let { ranges.add(it) }
            return RangeServerDispatcher(payload).dispatch(request)
        }
    }
}
