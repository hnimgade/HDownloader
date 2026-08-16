package com.hdownloader.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hdownloader.core.download.model.DownloadStatus

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val url: String,
    val finalUrl: String? = null,
    val fileName: String,
    val filePath: String? = null,
    val mimeType: String? = null,
    val extension: String? = null,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val speed: Long = 0L,
    val eta: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val errorMessage: String? = null,
    val categoryId: Long? = null,
    val thumbnailUri: String? = null,
    val isPaused: Boolean = false,
    val isSelected: Boolean = false,
    val retryCount: Int = 0,
    val supportsRange: Boolean = false,
    val connectionCount: Int = 1,
)
