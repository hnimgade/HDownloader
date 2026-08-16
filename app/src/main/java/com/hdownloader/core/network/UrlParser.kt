package com.hdownloader.core.network

import java.net.URI
import java.net.URLDecoder

/**
 * Result of parsing a download URL.
 */
data class ParsedUrl(
    val cleanUrl: String,
    val host: String?,
    val fileName: String,
    val extension: String?,
)

/**
 * Lightweight URL parsing and validation. No network calls are performed here.
 */
object UrlParser {

    fun isValid(raw: String): Boolean {
        val trimmed = raw.trim()
        val isHttp = trimmed.startsWith("http://") || trimmed.startsWith("https://")
        if (!isHttp) return false
        return runCatching { URI(trimmed) }.isSuccess
    }

    fun parse(raw: String): ParsedUrl {
        val clean = raw.trim()
        val uri = runCatching { URI(clean) }.getOrNull()
        val host = uri?.host
        val path = uri?.path.orEmpty()
        val lastSegment = path.substringAfterLast('/')
            .takeIf { it.isNotBlank() && it != "/" }
            ?: DEFAULT_FILE_NAME

        val decoded = runCatching { URLDecoder.decode(lastSegment, Charsets.UTF_8.name()) }
            .getOrDefault(lastSegment)

        val extension = decoded.substringAfterLast('.', missingDelimiterValue = "")
            .takeIf { it.isNotEmpty() && it.length <= 10 && it.all { c -> c.isLetterOrDigit() } }

        val fileName = if (extension != null && decoded.length > extension.length + 1) {
            decoded
        } else {
            "$decoded$DEFAULT_EXTENSION"
        }

        return ParsedUrl(
            cleanUrl = clean,
            host = host,
            fileName = fileName,
            extension = extension,
        )
    }

    fun host(raw: String): String? = runCatching { URI(raw.trim()) }.getOrNull()?.host

    private const val DEFAULT_FILE_NAME = "download"
    private const val DEFAULT_EXTENSION = ".bin"
}
