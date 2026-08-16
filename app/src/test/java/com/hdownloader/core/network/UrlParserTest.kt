package com.hdownloader.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlParserTest {

    @Test
    fun `valid http url is accepted`() {
        assertTrue(UrlParser.isValid("http://example.com/file.zip"))
    }

    @Test
    fun `valid https url is accepted`() {
        assertTrue(UrlParser.isValid("https://example.com/video.mp4"))
    }

    @Test
    fun `ftp url is rejected`() {
        assertFalse(UrlParser.isValid("ftp://example.com/file"))
    }

    @Test
    fun `blank input is rejected`() {
        assertFalse(UrlParser.isValid(""))
        assertFalse(UrlParser.isValid("   "))
    }

    @Test
    fun `non url text is rejected`() {
        assertFalse(UrlParser.isValid("not a url"))
    }

    @Test
    fun `extracts filename from path`() {
        val parsed = UrlParser.parse("https://example.com/downloads/movie.mp4")
        assertEquals("movie.mp4", parsed.fileName)
        assertEquals("mp4", parsed.extension)
        assertEquals("example.com", parsed.host)
    }

    @Test
    fun `query string does not leak into filename`() {
        val parsed = UrlParser.parse("https://example.com/file.zip?token=abc&expires=1")
        assertEquals("file.zip", parsed.fileName)
    }

    @Test
    fun `falls back to a default filename when path is empty`() {
        val parsed = UrlParser.parse("https://example.com")
        assertTrue(parsed.fileName.isNotBlank())
    }

    @Test
    fun `host extraction works`() {
        assertEquals("cdn.example.org", UrlParser.host("https://cdn.example.org/a/b.mp3"))
        assertNull(UrlParser.host("not-a-url"))
    }
}
