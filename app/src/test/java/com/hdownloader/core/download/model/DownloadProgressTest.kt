package com.hdownloader.core.download.model

import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadProgressTest {

    @Test
    fun `percent is calculated correctly`() {
        val progress = DownloadProgress(downloadedBytes = 25, totalBytes = 100)
        assertEquals(25, progress.percent)
    }

    @Test
    fun `percent is capped at 100`() {
        val progress = DownloadProgress(downloadedBytes = 120, totalBytes = 100)
        assertEquals(100, progress.percent)
    }

    @Test
    fun `percent is zero when total is unknown`() {
        val progress = DownloadProgress(downloadedBytes = 50, totalBytes = 0)
        assertEquals(0, progress.percent)
    }

    @Test
    fun `download state progress clamps to valid range`() {
        val state = DownloadState(totalBytes = 200, downloadedBytes = 100)
        assertEquals(0.5f, state.progress, 0.001f)
    }

    @Test
    fun `download state progress is zero for empty file`() {
        val state = DownloadState(totalBytes = 0)
        assertEquals(0f, state.progress, 0.001f)
    }

    @Test
    fun `source url prefers final url when present`() {
        val state = DownloadState(
            url = "https://example.com/dl?id=1",
            finalUrl = "https://cdn.example.com/file.mp4",
        )
        assertEquals("https://cdn.example.com/file.mp4", state.sourceUrl)
    }

    @Test
    fun `source url falls back to original url`() {
        val state = DownloadState(url = "https://example.com/dl?id=1")
        assertEquals("https://example.com/dl?id=1", state.sourceUrl)
    }
}
