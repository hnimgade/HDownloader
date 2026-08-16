package com.hdownloader.core.browser.repository

import com.hdownloader.core.browser.model.BrowserHistoryEntry
import kotlinx.coroutines.flow.Flow

interface BrowserHistoryRepository {

    fun observeAll(): Flow<List<BrowserHistoryEntry>>

    suspend fun record(url: String, title: String?)

    suspend fun clear()
}
