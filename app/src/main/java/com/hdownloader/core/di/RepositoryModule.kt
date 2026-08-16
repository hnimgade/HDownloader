package com.hdownloader.core.di

import com.hdownloader.core.bookmark.repository.BookmarkRepository
import com.hdownloader.core.bookmark.repository.RoomBookmarkRepository
import com.hdownloader.core.browser.repository.BrowserHistoryRepository
import com.hdownloader.core.browser.repository.RoomBrowserHistoryRepository
import com.hdownloader.core.category.repository.CategoryRepository
import com.hdownloader.core.category.repository.RoomCategoryRepository
import com.hdownloader.core.download.repository.DownloadRepository
import com.hdownloader.core.download.repository.RoomDownloadRepository
import com.hdownloader.core.download.storage.DownloadStorage
import com.hdownloader.core.download.storage.FileDownloadStorage
import com.hdownloader.core.history.repository.DownloadHistoryRepository
import com.hdownloader.core.history.repository.RoomDownloadHistoryRepository
import com.hdownloader.core.media.repository.MediaRepository
import com.hdownloader.core.media.repository.RoomMediaRepository
import com.hdownloader.core.playlist.repository.PlaylistRepository
import com.hdownloader.core.playlist.repository.RoomPlaylistRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(
        impl: RoomDownloadRepository,
    ): DownloadRepository

    @Binds
    @Singleton
    abstract fun bindDownloadStorage(
        impl: FileDownloadStorage,
    ): DownloadStorage

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: RoomCategoryRepository,
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        impl: RoomMediaRepository,
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(
        impl: RoomPlaylistRepository,
    ): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindDownloadHistoryRepository(
        impl: RoomDownloadHistoryRepository,
    ): DownloadHistoryRepository

    @Binds
    @Singleton
    abstract fun bindBrowserHistoryRepository(
        impl: RoomBrowserHistoryRepository,
    ): BrowserHistoryRepository

    @Binds
    @Singleton
    abstract fun bindBookmarkRepository(
        impl: RoomBookmarkRepository,
    ): BookmarkRepository
}
