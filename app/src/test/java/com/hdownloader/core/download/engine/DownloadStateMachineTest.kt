package com.hdownloader.core.download.engine

import com.hdownloader.core.download.model.DownloadStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DownloadStateMachineTest {

    @Test
    fun `legal transition is accepted`() {
        assertEquals(
            DownloadStatus.PREPARING,
            DownloadStateMachine.transition(DownloadStatus.QUEUED, DownloadStatus.PREPARING),
        )
    }

    @Test(expected = IllegalStateException::class)
    fun `illegal transition is rejected`() {
        DownloadStateMachine.transition(DownloadStatus.COMPLETED, DownloadStatus.DOWNLOADING)
    }

    @Test
    fun `terminal states cannot transition`() {
        assertFalse(DownloadStateMachine.canTransition(DownloadStatus.COMPLETED, DownloadStatus.QUEUED))
        assertFalse(DownloadStateMachine.canTransition(DownloadStatus.CANCELLED, DownloadStatus.QUEUED))
    }

    @Test
    fun `same state transition is a no-op`() {
        assertTrue(DownloadStateMachine.canTransition(DownloadStatus.DOWNLOADING, DownloadStatus.DOWNLOADING))
    }

    @Test
    fun `full happy path is valid`() {
        val path = listOf(
            DownloadStatus.QUEUED,
            DownloadStatus.PREPARING,
            DownloadStatus.DOWNLOADING,
            DownloadStatus.COMPLETING,
            DownloadStatus.COMPLETED,
        )
        path.zipWithNext().forEach { (from, to) ->
            assertTrue("$from -> $to should be legal", DownloadStateMachine.canTransition(from, to))
        }
    }

    @Test
    fun `pause and resume cycle is valid`() {
        assertTrue(DownloadStateMachine.canTransition(DownloadStatus.DOWNLOADING, DownloadStatus.PAUSED))
        assertTrue(DownloadStateMachine.canTransition(DownloadStatus.PAUSED, DownloadStatus.QUEUED))
    }

    @Test
    fun `retry from failed is valid`() {
        assertTrue(DownloadStateMachine.canTransition(DownloadStatus.FAILED, DownloadStatus.QUEUED))
    }
}
