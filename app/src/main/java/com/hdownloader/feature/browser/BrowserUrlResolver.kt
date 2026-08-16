package com.hdownloader.feature.browser

import java.net.URLDecoder
import java.net.URLEncoder

/**
 * Pure URL logic for the browser: turns address-bar input into a loadable
 * URL and detects download/media links. Kept dependency-free so it can be
 * unit tested without Android.
 */
object BrowserUrlResolver {

    private const val SEARCH_BASE = "https://www.google.com/search?q="

    /** Resolves raw address-bar input into a URL to load. */
    fun resolve(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""
        return when {
            trimmed.startsWith("http://") || trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("www.") -> "https://$trimmed"
            looksLikeQuery(trimmed) -> SEARCH_BASE + URLEncoder.encode(trimmed, Charsets.UTF_8.name())
            else -> "https://$trimmed"
        }
    }

    /**
     * Returns a usable file name for a detected download link, preferring the
     * Content-Disposition header and falling back to the URL path segment.
     */
    fun fileNameFor(url: String, contentDisposition: String?, mimeType: String?): String {
        contentDisposition?.let { disposition ->
            val fromHeader = parseContentDispositionFileName(disposition)
            if (!fromHeader.isNullOrBlank()) return fromHeader
        }
        val path = runCatching { java.net.URI(url).path }.getOrNull().orEmpty()
        val segment = path.substringAfterLast('/')
            .takeIf { it.isNotBlank() && it != "/" }
            ?: return fallbackFileName(mimeType)
        val decoded = runCatching { URLDecoder.decode(segment, Charsets.UTF_8.name()) }
            .getOrDefault(segment)
        return if (decoded.contains('.')) decoded else "$decoded${extensionFor(mimeType)}"
    }

    /** True when the URL looks like a direct media file link worth intercepting. */
    fun isMediaUrl(url: String): Boolean {
        val host = runCatching { java.net.URI(url).host }.getOrNull() ?: return false
        val isHttp = url.startsWith("http://") || url.startsWith("https://")
        if (!isHttp) return false
        val path = runCatching { java.net.URI(url).path }.getOrNull().orEmpty()
        val extension = path.substringAfterLast('.', "").lowercase()
        if (extension.isBlank() || extension == path.lowercase()) return false
        return extension in MEDIA_EXTENSIONS
    }

    fun parseContentDispositionFileName(value: String): String? {
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

    private fun looksLikeQuery(trimmed: String): Boolean =
        trimmed.contains(' ') || !trimmed.contains('.')

    private fun fallbackFileName(mimeType: String?): String =
        "download${extensionFor(mimeType)}"

    private fun extensionFor(mimeType: String?): String = when (mimeType?.substringBefore(';')?.lowercase()) {
        "video/mp4" -> ".mp4"
        "video/webm" -> ".webm"
        "video/x-matroska" -> ".mkv"
        "audio/mpeg" -> ".mp3"
        "audio/mp4", "audio/x-m4a" -> ".m4a"
        "audio/ogg" -> ".ogg"
        "audio/wav" -> ".wav"
        "application/pdf" -> ".pdf"
        "image/jpeg" -> ".jpg"
        "image/png" -> ".png"
        "image/gif" -> ".gif"
        else -> ".bin"
    }

    private val MEDIA_EXTENSIONS = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "ts", "3gp", "mpg", "mpeg",
        "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus", "mid", "midi", "amr",
        "zip", "rar", "7z", "tar", "gz", "pdf", "apk",
    )
}
