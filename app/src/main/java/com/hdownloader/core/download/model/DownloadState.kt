package com.hdownloader.core.download.model

/**
 * Immutable UI-facing snapshot of a single download.
 */
data class DownloadState(
    val id: Long = 0L,
    val url: String = "",
    val finalUrl: String? = null,
    val fileName: String = "",
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val speed: Long = 0L,
    val eta: Long = 0L,
    val supportsRange: Boolean = false,
    val connectionCount: Int = 1,
    val errorMessage: String? = null,
) {
    /** Address the file was fetched from, preferring the post-redirect URL. */
    val sourceUrl: String
        get() = finalUrl?.takeIf { it.isNotBlank() } ?: url

    val progress: Float
        get() = if (totalBytes > 0) {
            (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f)
        } else {
            0f
        }

    val isActive: Boolean get() = status.isActive
    val isTerminal: Boolean get() = status.isTerminal
}
