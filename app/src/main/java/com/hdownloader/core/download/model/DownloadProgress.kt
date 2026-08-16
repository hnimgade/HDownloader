package com.hdownloader.core.download.model

/**
 * High-level progress used by the UI, throttled upstream by the engine.
 */
data class DownloadProgress(
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L,
    val speed: Long = 0L,
    val eta: Long = 0L,
) {
    val percent: Int
        get() = if (totalBytes > 0) {
            ((downloadedBytes.toFloat() / totalBytes) * 100).toInt().coerceIn(0, 100)
        } else {
            0
        }
}
