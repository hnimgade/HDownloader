package com.hdownloader.core.download

import com.hdownloader.core.download.engine.HttpDownloader
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okio.Buffer

/**
 * MockWebServer dispatcher that honours HEAD and byte-range requests against a
 * fixed in-memory payload, mirroring a real HTTP file server.
 */
class RangeServerDispatcher(
    private val payload: ByteArray,
    private val acceptsRanges: Boolean = true,
) : okhttp3.mockwebserver.Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse {
        val range = request.getHeader("Range")
        val rangeMatch = Regex("bytes=(\\d+)-(\\d*)").find(range.orEmpty())
        return when {
            request.method == "HEAD" -> MockResponse()
                .setHeader("Accept-Ranges", if (acceptsRanges) "bytes" else "none")
                .setHeader("Content-Length", payload.size.toString())
                .setHeader("Content-Type", "application/octet-stream")

            acceptsRanges && rangeMatch != null -> {
                val start = rangeMatch.groupValues[1].toLong()
                val endRaw = rangeMatch.groupValues[2]
                val end = if (endRaw.isEmpty()) {
                    payload.size - 1L
                } else {
                    endRaw.toLong().coerceAtMost(payload.size - 1L)
                }
                val slice = payload.copyOfRange(start.toInt(), end.toInt() + 1)
                MockResponse()
                    .setResponseCode(206)
                    .setHeader("Accept-Ranges", "bytes")
                    .setHeader("Content-Range", "bytes $start-$end/${payload.size}")
                    .setHeader("Content-Type", "application/octet-stream")
                    .setBody(Buffer().write(slice))
            }

            else -> MockResponse()
                .setResponseCode(200)
                .setBody(Buffer().write(payload))
        }
    }
}

/** Serves [payload] to any request; used to exercise plain (non-range) servers. */
class PlainServerDispatcher(private val payload: ByteArray) : okhttp3.mockwebserver.Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Length", payload.size.toString())
            .setBody(Buffer().write(payload))
}

/** Fails every request; used to exercise error handling. */
class FailingServerDispatcher : okhttp3.mockwebserver.Dispatcher() {

    override fun dispatch(request: RecordedRequest): MockResponse =
        MockResponse().setResponseCode(503).setBody("unavailable")
}
