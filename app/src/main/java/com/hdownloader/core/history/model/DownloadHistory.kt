package com.hdownloader.core.history.model

import com.hdownloader.core.download.model.DownloadStatus

data class DownloadHistory(
    val id: Long = 0L,
    val url: String,
    val fileName: String,
    val mimeType: String? = null,
    val size: Long = 0L,
    val sourceHost: String? = null,
    val status: DownloadStatus = DownloadStatus.COMPLETED,
    val downloadedAt: Long = System.currentTimeMillis(),
)
