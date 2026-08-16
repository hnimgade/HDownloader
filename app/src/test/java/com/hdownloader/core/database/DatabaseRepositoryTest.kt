package com.hdownloader.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.hdownloader.core.bookmark.model.Bookmark
import com.hdownloader.core.bookmark.repository.BookmarkRepository
import com.hdownloader.core.bookmark.repository.RoomBookmarkRepository
import com.hdownloader.core.browser.model.BrowserHistoryEntry
import com.hdownloader.core.browser.repository.BrowserHistoryRepository
import com.hdownloader.core.browser.repository.RoomBrowserHistoryRepository
import com.hdownloader.core.category.repository.CategoryRepository
import com.hdownloader.core.category.repository.RoomCategoryRepository
import com.hdownloader.core.download.model.DownloadStatus
import com.hdownloader.core.download.repository.DownloadRepository
import com.hdownloader.core.download.repository.RoomDownloadRepository
import com.hdownloader.core.history.model.DownloadHistory
import com.hdownloader.core.history.repository.DownloadHistoryRepository
import com.hdownloader.core.history.repository.RoomDownloadHistoryRepository
import com.hdownloader.core.media.model.Media
import com.hdownloader.core.media.model.MediaCategory
import com.hdownloader.core.media.repository.MediaRepository
import com.hdownloader.core.media.repository.RoomMediaRepository
import com.hdownloader.core.playlist.model.NewPlaylistItem
import com.hdownloader.core.playlist.repository.PlaylistRepository
import com.hdownloader.core.playlist.repository.RoomPlaylistRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DatabaseRepositoryTest {

    private lateinit var database: HDownloaderDatabase
    private lateinit var downloadRepository: DownloadRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var mediaRepository: MediaRepository
    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var historyRepository: DownloadHistoryRepository
    private lateinit var browserHistoryRepository: BrowserHistoryRepository
    private lateinit var bookmarkRepository: BookmarkRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HDownloaderDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        downloadRepository = RoomDownloadRepository(database.downloadDao())
        categoryRepository = RoomCategoryRepository(database.categoryDao())
        mediaRepository = RoomMediaRepository(database.mediaDao())
        playlistRepository = RoomPlaylistRepository(database.playlistDao())
        historyRepository = RoomDownloadHistoryRepository(database.downloadHistoryDao())
        browserHistoryRepository = RoomBrowserHistoryRepository(database.browserHistoryDao())
        bookmarkRepository = RoomBookmarkRepository(database.bookmarkDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun defaultCategoriesAreSeededOnce() = runTest {
        categoryRepository.ensureDefaultCategories()
        categoryRepository.ensureDefaultCategories()
        val categories = categoryRepository.getAll()
        assertEquals(5, categories.size)
        assertTrue(categories.map { it.name }.containsAll(listOf("Videos", "Music", "Images", "Documents", "Other")))
    }

    @Test
    fun downloadLifecyclePersistsStates() = runTest {
        val id = downloadRepository.addDownload(
            url = "https://example.com/file.zip",
            fileName = "file.zip",
            categoryId = null,
            startImmediately = true,
        )
        var state = downloadRepository.observeById(id).first()
        assertEquals(DownloadStatus.QUEUED, state?.status)

        downloadRepository.pause(id)
        state = downloadRepository.observeById(id).first()
        assertEquals(DownloadStatus.PAUSED, state?.status)

        downloadRepository.resume(id)
        state = downloadRepository.observeById(id).first()
        assertEquals(DownloadStatus.QUEUED, state?.status)

        downloadRepository.cancel(id)
        state = downloadRepository.observeById(id).first()
        assertEquals(DownloadStatus.CANCELLED, state?.status)
    }

    @Test
    fun removeDeletesDownload() = runTest {
        val id = downloadRepository.addDownload("https://example.com/a.mp4", "a.mp4")
        downloadRepository.remove(id)
        assertEquals(null, downloadRepository.observeById(id).first())
    }

    @Test
    fun mediaUpsertAndDelete() = runTest {
        val id = mediaRepository.upsert(
            Media(fileName = "video.mp4", category = MediaCategory.VIDEO, size = 1024),
        )
        val all = mediaRepository.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("video.mp4", all.first().fileName)

        val videos = mediaRepository.observeByCategory(MediaCategory.VIDEO).first()
        assertEquals(1, videos.size)

        mediaRepository.delete(id)
        assertEquals(0, mediaRepository.observeAll().first().size)
    }

    @Test
    fun playlistCreateAddReorderRenameDelete() = runTest {
        val playlistId = playlistRepository.create("Favorites")
        assertEquals(1, playlistRepository.observePlaylists().first().size)

        playlistRepository.addItems(
            playlistId,
            listOf(NewPlaylistItem(title = "song A"), NewPlaylistItem(title = "song B")),
        )
        var items = playlistRepository.observeItems(playlistId).first()
        assertEquals(2, items.size)

        val ordered = items.reversed().map { it.id }
        playlistRepository.reorder(playlistId, ordered)
        items = playlistRepository.observeItems(playlistId).first()
        assertEquals(0, items.first().position)
        assertEquals(1, items.last().position)
        assertEquals("song B", items.first().title)

        playlistRepository.rename(playlistId, "Renamed")
        val playlists = playlistRepository.observePlaylists().first()
        assertEquals("Renamed", playlists.first().name)

        playlistRepository.delete(playlistId)
        assertEquals(0, playlistRepository.observePlaylists().first().size)
        assertEquals(0, playlistRepository.observeItems(playlistId).first().size)
    }

    @Test
    fun historyRecordAndClear() = runTest {
        historyRepository.record(
            DownloadHistory(
                url = "https://example.com/h.zip",
                fileName = "h.zip",
                size = 2048,
                sourceHost = "example.com",
            ),
        )
        assertEquals(1, historyRepository.observeAll().first().size)
        historyRepository.clear()
        assertEquals(0, historyRepository.observeAll().first().size)
    }

    @Test
    fun browserHistoryRecordAndClear() = runTest {
        browserHistoryRepository.record("https://example.com", "Example")
        browserHistoryRepository.record("https://example.org", "Example Org")
        assertEquals(2, browserHistoryRepository.observeAll().first().size)
        browserHistoryRepository.clear()
        assertEquals(0, browserHistoryRepository.observeAll().first().size)
    }

    @Test
    fun bookmarksAreDeduplicated() = runTest {
        bookmarkRepository.add("https://example.com/a", "A")
        bookmarkRepository.add("https://example.com/a", "A again")
        assertEquals(1, bookmarkRepository.observeAll().first().size)
        assertTrue(bookmarkRepository.isBookmarked("https://example.com/a"))
        bookmarkRepository.remove(bookmarkRepository.observeAll().first().first().id)
        assertEquals(0, bookmarkRepository.observeAll().first().size)
    }
}
