package com.hdownloader.core.download.model

/**
 * A unit of work within a download; used when the server supports byte-range
 * requests and the file is downloaded over multiple connections.
 */
data class DownloadPartState(
    val partIndex: Int = 0,
    val startByte: Long = 0L,
    val endByte: Long = 0L,
    val currentByte: Long = 0L,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val retryCount: Int = 0,
)
