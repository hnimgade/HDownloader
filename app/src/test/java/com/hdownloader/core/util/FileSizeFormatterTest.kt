package com.hdownloader.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FileSizeFormatterTest {

    @Test
    fun `bytes under 1KB are shown as bytes`() {
        assertEquals("512 B", FileSizeFormatter.format(512))
    }

    @Test
    fun `kilobytes are formatted`() {
        assertEquals("1.5 KB", FileSizeFormatter.format(1536))
    }

    @Test
    fun `megabytes are formatted`() {
        assertEquals("1.0 MB", FileSizeFormatter.format(1024 * 1024))
    }

    @Test
    fun `gigabytes are formatted`() {
        assertEquals("2.0 GB", FileSizeFormatter.format(2L * 1024 * 1024 * 1024))
    }

    @Test
    fun `zero bytes are formatted`() {
        assertEquals("0 B", FileSizeFormatter.format(0))
    }
}
