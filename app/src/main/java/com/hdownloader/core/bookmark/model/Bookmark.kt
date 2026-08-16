package com.hdownloader.core.bookmark.model

data class Bookmark(
    val id: Long = 0L,
    val url: String,
    val title: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)
