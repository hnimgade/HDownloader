package com.hdownloader.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hdownloader.core.download.model.DownloadStatus

@Entity(tableName = "download_history")
data class DownloadHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val url: String,
    val fileName: String,
    val mimeType: String? = null,
    val size: Long = 0L,
    val sourceHost: String? = null,
    val status: DownloadStatus = DownloadStatus.COMPLETED,
    val downloadedAt: Long = System.currentTimeMillis(),
)
