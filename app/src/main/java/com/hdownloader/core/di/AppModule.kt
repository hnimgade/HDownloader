package com.hdownloader.core.di

import android.content.Context
import com.hdownloader.core.database.HDownloaderDatabase
import com.hdownloader.core.database.dao.BookmarkDao
import com.hdownloader.core.database.dao.BrowserHistoryDao
import com.hdownloader.core.database.dao.CategoryDao
import com.hdownloader.core.database.dao.DownloadDao
import com.hdownloader.core.database.dao.DownloadHistoryDao
import com.hdownloader.core.database.dao.DownloadPartDao
import com.hdownloader.core.database.dao.MediaDao
import com.hdownloader.core.database.dao.PlaylistDao
import com.hdownloader.core.network.HttpClientProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): HDownloaderDatabase = HDownloaderDatabase.build(context)

    @Provides
    fun provideDownloadDao(database: HDownloaderDatabase): DownloadDao = database.downloadDao()

    @Provides
    fun provideDownloadPartDao(database: HDownloaderDatabase): DownloadPartDao = database.downloadPartDao()

    @Provides
    fun provideDownloadHistoryDao(database: HDownloaderDatabase): DownloadHistoryDao = database.downloadHistoryDao()

    @Provides
    fun provideCategoryDao(database: HDownloaderDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun providePlaylistDao(database: HDownloaderDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun provideBrowserHistoryDao(database: HDownloaderDatabase): BrowserHistoryDao = database.browserHistoryDao()

    @Provides
    fun provideBookmarkDao(database: HDownloaderDatabase): BookmarkDao = database.bookmarkDao()

    @Provides
    fun provideMediaDao(database: HDownloaderDatabase): MediaDao = database.mediaDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = HttpClientProvider.create()
}
