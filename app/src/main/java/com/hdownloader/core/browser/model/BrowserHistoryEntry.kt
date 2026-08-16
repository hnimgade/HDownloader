package com.hdownloader.core.browser.model

data class BrowserHistoryEntry(
    val id: Long = 0L,
    val url: String,
    val title: String? = null,
    val visitedAt: Long = System.currentTimeMillis(),
)
