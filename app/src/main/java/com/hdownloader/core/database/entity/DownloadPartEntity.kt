package com.hdownloader.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.hdownloader.core.download.model.DownloadStatus

@Entity(
    tableName = "download_parts",
    foreignKeys = [
        ForeignKey(
            entity = DownloadEntity::class,
            parentColumns = ["id"],
            childColumns = ["downloadId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("downloadId")],
)
data class DownloadPartEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val downloadId: Long,
    val partIndex: Int,
    val startByte: Long,
    val endByte: Long,
    val currentByte: Long = startByte,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val retryCount: Int = 0,
)
