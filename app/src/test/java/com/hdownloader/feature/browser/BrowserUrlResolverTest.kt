package com.hdownloader.feature.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BrowserUrlResolverTest {

    @Test
    fun `keeps https url as is`() {
        assertEquals("https://example.com/video.mp4", BrowserUrlResolver.resolve("https://example.com/video.mp4"))
    }

    @Test
    fun `keeps http url as is`() {
        assertEquals("http://example.com/file.zip", BrowserUrlResolver.resolve("http://example.com/file.zip"))
    }

    @Test
    fun `adds https scheme to bare domain`() {
        assertEquals("https://example.com", BrowserUrlResolver.resolve("example.com"))
    }

    @Test
    fun `adds https scheme to www domain`() {
        assertEquals("https://www.example.com", BrowserUrlResolver.resolve("www.example.com"))
    }

    @Test
    fun `turns multi word input into a search query`() {
        val resolved = BrowserUrlResolver.resolve("hello world")
        assertTrue(resolved.startsWith("https://www.google.com/search?q="))
        assertTrue(resolved.contains("hello"))
        assertTrue(resolved.contains("world"))
    }

    @Test
    fun `blank input resolves to empty`() {
        assertEquals("", BrowserUrlResolver.resolve("   "))
    }

    @Test
    fun `detects mp4 media url`() {
        assertTrue(BrowserUrlResolver.isMediaUrl("https://cdn.example.com/movie.mp4"))
    }

    @Test
    fun `detects mp3 media url`() {
        assertTrue(BrowserUrlResolver.isMediaUrl("https://cdn.example.com/song.mp3?token=abc"))
    }

    @Test
    fun `does not flag ordinary page as media`() {
        assertFalse(BrowserUrlResolver.isMediaUrl("https://example.com/watch?v=123"))
    }

    @Test
    fun `does not flag non http scheme`() {
        assertFalse(BrowserUrlResolver.isMediaUrl("file:///android_asset/browser_home.html"))
    }

    @Test
    fun `file name prefers content disposition header`() {
        val name = BrowserUrlResolver.fileNameFor(
            url = "https://example.com/download?id=9",
            contentDisposition = "attachment; filename=\"final_clip.mp4\"",
            mimeType = "video/mp4",
        )
        assertEquals("final_clip.mp4", name)
    }

    @Test
    fun `file name falls back to url segment`() {
        val name = BrowserUrlResolver.fileNameFor(
            url = "https://example.com/media/clip.mp4?token=x",
            contentDisposition = null,
            mimeType = "video/mp4",
        )
        assertEquals("clip.mp4", name)
    }

    @Test
    fun `file name adds mime extension when segment has none`() {
        val name = BrowserUrlResolver.fileNameFor(
            url = "https://example.com/download/12345",
            contentDisposition = null,
            mimeType = "audio/mpeg",
        )
        assertEquals("12345.mp3", name)
    }

    @Test
    fun `decodes percent encoded file name`() {
        val name = BrowserUrlResolver.fileNameFor(
            url = "https://example.com/My%20Song.mp3",
            contentDisposition = null,
            mimeType = null,
        )
        assertEquals("My Song.mp3", name)
    }
}
