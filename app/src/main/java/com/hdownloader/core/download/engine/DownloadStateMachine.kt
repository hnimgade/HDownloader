package com.hdownloader.core.download.engine

import com.hdownloader.core.download.model.DownloadStatus

/**
 * Validates and performs transitions of the download state machine.
 *
 * The set of legal transitions is deliberately strict so that invalid UI
 * actions cannot corrupt download state.
 */
object DownloadStateMachine {

    private val transitions: Map<DownloadStatus, Set<DownloadStatus>> = mapOf(
        DownloadStatus.QUEUED to setOf(
            DownloadStatus.PREPARING,
            DownloadStatus.PAUSED,
            DownloadStatus.CANCELLED,
        ),
        DownloadStatus.PREPARING to setOf(
            DownloadStatus.DOWNLOADING,
            DownloadStatus.PAUSED,
            DownloadStatus.FAILED,
            DownloadStatus.CANCELLED,
        ),
        DownloadStatus.DOWNLOADING to setOf(
            DownloadStatus.PAUSED,
            DownloadStatus.COMPLETING,
            DownloadStatus.FAILED,
            DownloadStatus.CANCELLED,
        ),
        DownloadStatus.PAUSED to setOf(
            DownloadStatus.QUEUED,
            DownloadStatus.CANCELLED,
            DownloadStatus.FAILED,
        ),
        DownloadStatus.COMPLETING to setOf(
            DownloadStatus.COMPLETED,
            DownloadStatus.FAILED,
        ),
        DownloadStatus.COMPLETED to emptySet(),
        DownloadStatus.FAILED to setOf(
            DownloadStatus.QUEUED,
            DownloadStatus.CANCELLED,
        ),
        DownloadStatus.CANCELLED to emptySet(),
    )

    fun canTransition(from: DownloadStatus, to: DownloadStatus): Boolean =
        from == to || (transitions[from]?.contains(to) ?: false)

    /**
     * Returns the target state when the transition is legal, otherwise throws
     * an [IllegalStateException] describing the invalid transition.
     */
    fun transition(from: DownloadStatus, to: DownloadStatus): DownloadStatus {
        if (!canTransition(from, to)) {
            throw IllegalStateException("Invalid download state transition: $from -> $to")
        }
        return to
    }
}
