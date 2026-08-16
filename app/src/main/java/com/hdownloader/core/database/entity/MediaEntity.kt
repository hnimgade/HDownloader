package com.hdownloader.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hdownloader.core.media.model.MediaCategory

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fileName: String,
    val mediaStoreUri: String? = null,
    val filePath: String? = null,
    val mimeType: String? = null,
    val size: Long = 0L,
    val duration: Long? = null,
    val category: MediaCategory = MediaCategory.OTHER,
    val dateAdded: Long = System.currentTimeMillis(),
)
