package com.hdownloader.core.media.model

data class Media(
    val id: Long = 0L,
    val fileName: String,
    val mediaStoreUri: String? = null,
    val filePath: String? = null,
    val mimeType: String? = null,
    val size: Long = 0L,
    val duration: Long? = null,
    val category: MediaCategory = MediaCategory.OTHER,
    val dateAdded: Long = System.currentTimeMillis(),
)
