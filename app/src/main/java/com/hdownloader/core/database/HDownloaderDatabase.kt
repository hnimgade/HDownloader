package com.hdownloader.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hdownloader.core.database.converter.Converters
import com.hdownloader.core.database.dao.AppSettingDao
import com.hdownloader.core.database.dao.BookmarkDao
import com.hdownloader.core.database.dao.BrowserHistoryDao
import com.hdownloader.core.database.dao.CategoryDao
import com.hdownloader.core.database.dao.DownloadDao
import com.hdownloader.core.database.dao.DownloadHistoryDao
import com.hdownloader.core.database.dao.DownloadPartDao
import com.hdownloader.core.database.dao.MediaDao
import com.hdownloader.core.database.dao.PlaylistDao
import com.hdownloader.core.database.entity.AppSettingEntity
import com.hdownloader.core.database.entity.BookmarkEntity
import com.hdownloader.core.database.entity.BrowserHistoryEntity
import com.hdownloader.core.database.entity.CategoryEntity
import com.hdownloader.core.database.entity.DownloadEntity
import com.hdownloader.core.database.entity.DownloadHistoryEntity
import com.hdownloader.core.database.entity.DownloadPartEntity
import com.hdownloader.core.database.entity.MediaEntity
import com.hdownloader.core.database.entity.PlaylistEntity
import com.hdownloader.core.database.entity.PlaylistItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DownloadEntity::class,
        DownloadPartEntity::class,
        DownloadHistoryEntity::class,
        CategoryEntity::class,
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        BrowserHistoryEntity::class,
        BookmarkEntity::class,
        MediaEntity::class,
        AppSettingEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class HDownloaderDatabase : RoomDatabase() {

    abstract fun downloadDao(): DownloadDao
    abstract fun downloadPartDao(): DownloadPartDao
    abstract fun downloadHistoryDao(): DownloadHistoryDao
    abstract fun categoryDao(): CategoryDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun browserHistoryDao(): BrowserHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun mediaDao(): MediaDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        private const val DB_NAME = "h_downloader.db"

        fun build(context: Context): HDownloaderDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                HDownloaderDatabase::class.java,
                DB_NAME,
            ).build()
    }
}
