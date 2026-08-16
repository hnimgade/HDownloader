package com.hdownloader.core.download.engine

import com.hdownloader.core.media.model.MediaCategory

/** Maps file extensions (and MIME types) to media categories. */
object MediaCategorizer {

    private val videoExtensions = setOf(
        "mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "m4v", "ts", "3gp", "mpg", "mpeg",
    )
    private val audioExtensions = setOf(
        "mp3", "wav", "flac", "aac", "ogg", "m4a", "wma", "opus", "mid", "midi", "amr",
    )
    private val imageExtensions = setOf(
        "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "heic", "ico", "tiff",
    )
    private val documentExtensions = setOf(
        "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md", "csv", "rtf", "epub",
    )

    fun forFileName(fileName: String, mimeType: String? = null): MediaCategory {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        val type = mimeType.orEmpty().lowercase()
        return when {
            extension in videoExtensions || type.startsWith("video/") -> MediaCategory.VIDEO
            extension in audioExtensions || type.startsWith("audio/") -> MediaCategory.MUSIC
            extension in imageExtensions || type.startsWith("image/") -> MediaCategory.IMAGE
            extension in documentExtensions ||
                type in setOf("application/pdf", "text/plain", "text/markdown") -> MediaCategory.DOCUMENT
            else -> MediaCategory.OTHER
        }
    }
}
