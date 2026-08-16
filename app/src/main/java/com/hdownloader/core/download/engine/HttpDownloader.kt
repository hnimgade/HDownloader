package com.hdownloader.core.download.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URLDecoder
import javax.inject.Inject

/** Server metadata discovered before a transfer begins. */
data class HttpMetadata(
    val finalUrl: String,
    val totalBytes: Long,
    val acceptsRanges: Boolean,
    val mimeType: String?,
    val contentDispositionFileName: String?,
)

/**
 * Low-level HTTP layer for downloads. Metadata detection is two-stage:
 * a HEAD request first, falling back to a 1-byte ranged GET for servers
 * that reject HEAD. Streaming is exposed as blocking chunk callbacks so the
 * engine controls buffering, disk writes and cooperative cancellation.
 */
class HttpDownloader @Inject constructor(
    private val client: OkHttpClient,
) {

    fun fetchMetadata(url: String, userAgent: String): HttpMetadata {
        tryHead(url, userAgent)?.let { return it }
        return probeRange(url, userAgent)
    }

    private fun tryHead(url: String, userAgent: String): HttpMetadata? {
        val request = Request.Builder()
            .url(url)
            .header(USER_AGENT, userAgent)
            .header("Connection", "close")
            .head()
            .build()
        val response = runCatching { client.newCall(request).execute() }
            .getOrElse { return null }
        return response.use {
            if (it.code >= 500) {
                throw HttpDownloadException("HTTP ${it.code} during HEAD")
            }
            if (!it.isSuccessful) return@use null
            HttpMetadata(
                finalUrl = it.request.url.toString(),
                totalBytes = it.header("Content-Length")?.toLongOrNull() ?: 0L,
                acceptsRanges = it.header("Accept-Ranges") == "bytes",
                mimeType = it.header("Content-Type"),
                contentDispositionFileName = parseContentDisposition(it.header("Content-Disposition")),
            )
        }
    }

    private fun probeRange(url: String, userAgent: String): HttpMetadata {
        val request = Request.Builder()
            .url(url)
            .header(USER_AGENT, userAgent)
            .header("Connection", "close")
            .header("Range", "bytes=0-0")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw HttpDownloadException("HTTP ${response.code} during range probe")
            }
            val totalBytes = if (response.code == 206) {
                response.header("Content-Range")
                    ?.substringAfter("/")
                    ?.toLongOrNull()
                    ?: 0L
            } else {
                response.header("Content-Length")?.toLongOrNull() ?: 0L
            }
            return HttpMetadata(
                finalUrl = response.request.url.toString(),
                totalBytes = totalBytes,
                acceptsRanges = response.code == 206,
                mimeType = response.header("Content-Type"),
                contentDispositionFileName = parseContentDisposition(response.header("Content-Disposition")),
            )
        }
    }

    /**
     * Streams the requested byte range, invoking [onChunk] with each buffered
     * slice. Returns the number of bytes streamed. The caller supplies the
     * starting offset so segmented downloads can position their writer.
     */
    suspend fun streamRange(
        url: String,
        start: Long,
        end: Long?,
        userAgent: String,
        onChunk: suspend (bytes: ByteArray) -> Unit,
    ): Long = withContext(Dispatchers.IO) {
        val rangeHeader = if (end != null) "bytes=$start-$end" else "bytes=$start-"
        val request = Request.Builder()
            .url(url)
            .header(USER_AGENT, userAgent)
            .header("Range", rangeHeader)
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw HttpDownloadException("HTTP ${response.code} for range $rangeHeader")
            }
            var read = 0L
            response.body?.use { body ->
                val buffer = ByteArray(BUFFER_SIZE)
                while (true) {
                    val n = body.source().read(buffer)
                    if (n <= 0) break
                    onChunk(buffer.copyOf(n))
                    read += n
                }
            }
            read
        }
    }

    private fun parseContentDisposition(value: String?): String? {
        if (value.isNullOrBlank()) return null
        val starMatch = Regex("filename\\*=(?:UTF-8'')?([^;]+)").find(value)
        if (starMatch != null) {
            val decoded = runCatching {
                URLDecoder.decode(starMatch.groupValues[1].trim('"', ' '), Charsets.UTF_8.name())
            }.getOrNull()
            if (!decoded.isNullOrBlank()) return decoded
        }
        val plainMatch = Regex("filename=\"?([^\";]+)\"?").find(value)
        return plainMatch?.groupValues?.get(1)?.trim()
    }

    class HttpDownloadException(message: String) : IOException(message)

    private companion object {
        const val USER_AGENT = "User-Agent"
        const val BUFFER_SIZE = 8192
    }
}
