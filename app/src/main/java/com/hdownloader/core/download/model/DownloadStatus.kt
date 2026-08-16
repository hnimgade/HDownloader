package com.hdownloader.core.download.model

/**
 * Lifecycle of a download, as defined by the product spec.
 */
enum class DownloadStatus {
    QUEUED,
    PREPARING,
    DOWNLOADING,
    PAUSED,
    COMPLETING,
    COMPLETED,
    FAILED,
    CANCELLED,
    ;

    val isActive: Boolean
        get() = this == DOWNLOADING || this == PREPARING || this == COMPLETING

    val isTerminal: Boolean
        get() = this == COMPLETED || this == CANCELLED

    val isPausable: Boolean
        get() = this == QUEUED || this == DOWNLOADING || this == PREPARING

    val isResumable: Boolean
        get() = this == PAUSED || this == FAILED
}
