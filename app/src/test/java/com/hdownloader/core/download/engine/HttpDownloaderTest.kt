package com.hdownloader.core.download.engine

import com.hdownloader.core.download.RangeServerDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HttpDownloaderTest {

    private lateinit var server: MockWebServer
    private lateinit var downloader: HttpDownloader

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        downloader = HttpDownloader(OkHttpClient())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun url(path: String) = server.url(path).toString()

    @Test
    fun headReturnsMetadata() {
        val payload = ByteArray(4096) { it.toByte() }
        server.dispatcher = RangeServerDispatcher(payload)
        val metadata = downloader.fetchMetadata(url("/file.bin"), USER_AGENT)

        assertEquals(4096L, metadata.totalBytes)
        assertTrue(metadata.acceptsRanges)
        assertEquals(url("/file.bin"), metadata.finalUrl)
    }

    @Test
    fun contentDispositionProvidesFileName() {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Accept-Ranges", "bytes")
                .setHeader("Content-Length", "10")
                .setHeader("Content-Disposition", "attachment; filename=\"report.pdf\"")
                .setBody("0123456789"),
        )
        val metadata = downloader.fetchMetadata(url("/download"), USER_AGENT)

        assertEquals("report.pdf", metadata.contentDispositionFileName)
    }

    @Test
    fun fallsBackToRangeProbeWhenHeadRejected() {
        server.dispatcher = object : okhttp3.mockwebserver.Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.method == "HEAD") {
                    return MockResponse().setResponseCode(405)
                }
                if (request.getHeader("Range") == "bytes=0-0") {
                    return MockResponse()
                        .setResponseCode(206)
                        .setHeader("Content-Range", "bytes 0-0/2048")
                        .setHeader("Accept-Ranges", "bytes")
                        .setBody("x")
                }
                return MockResponse().setResponseCode(200).setBody("payload")
            }
        }

        val metadata = downloader.fetchMetadata(url("/probe.bin"), USER_AGENT)

        assertEquals(2048L, metadata.totalBytes)
        assertTrue(metadata.acceptsRanges)
    }

    @Test
    fun streamsRequestedRange() = runBlocking {
        val payload = ByteArray(10_000) { (it % 251).toByte() }
        server.dispatcher = RangeServerDispatcher(payload)

        val collected = ArrayList<ByteArray>()
        val total = downloader.streamRange(
            url = url("/big.bin"),
            start = 1_000L,
            end = 1_999L,
            userAgent = USER_AGENT,
        ) { chunk -> collected.add(chunk) }

        assertEquals(1_000L, total)
        val merged = collected.flatMap { it.toList() }
        assertEquals(payload.copyOfRange(1_000, 2_000).toList(), merged)
    }

    @Test
    fun openEndedRangeStreamsToEnd() = runBlocking {
        val payload = ByteArray(5_000) { 1 }
        server.dispatcher = RangeServerDispatcher(payload)

        var received = 0L
        val total = downloader.streamRange(
            url = url("/open.bin"),
            start = 4_000L,
            end = null,
            userAgent = USER_AGENT,
        ) { chunk -> received += chunk.size }

        assertEquals(1_000L, total)
        assertEquals(1_000L, received)
    }

    @Test
    fun headFollowedByRangeGetStreamsBytes() = runBlocking {
        val payload = ByteArray(16_000) { 9 }
        server.dispatcher = RangeServerDispatcher(payload)

        val metadata = downloader.fetchMetadata(url("/seq.bin"), USER_AGENT)
        assertEquals(16_000L, metadata.totalBytes)

        var received = 0L
        val total = downloader.streamRange(
            url = url("/seq.bin"),
            start = 0L,
            end = 15_999L,
            userAgent = USER_AGENT,
        ) { chunk -> received += chunk.size }

        assertEquals("HEAD-then-GET should stream the full range", 16_000L, total)
        assertEquals(16_000L, received)
    }

    @Test
    fun concurrentRangesStreamCorrectly() = runBlocking {
        val payload = ByteArray(100_000) { (it % 251).toByte() }
        server.dispatcher = RangeServerDispatcher(payload)

        val total = coroutineScope {
            listOf(
                async(Dispatchers.IO) { streamAll(downloader, url("/conc.bin"), 0L, 24_999L) },
                async(Dispatchers.IO) { streamAll(downloader, url("/conc.bin"), 25_000L, 49_999L) },
                async(Dispatchers.IO) { streamAll(downloader, url("/conc.bin"), 50_000L, 74_999L) },
                async(Dispatchers.IO) { streamAll(downloader, url("/conc.bin"), 75_000L, 99_999L) },
            ).awaitAll().sum()
        }

        assertEquals(100_000L, total)
    }

    private suspend fun streamAll(d: HttpDownloader, url: String, start: Long, end: Long): Long {
        var received = 0L
        val t = d.streamRange(url, start, end, USER_AGENT) { chunk -> received += chunk.size }
        return t
    }

    private companion object {
        const val USER_AGENT = "HDownloaderTest/1.0"
    }
}
