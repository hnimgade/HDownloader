package com.hdownloader.core.history.repository

import com.hdownloader.core.history.model.DownloadHistory
import kotlinx.coroutines.flow.Flow

interface DownloadHistoryRepository {

    fun observeAll(): Flow<List<DownloadHistory>>

    suspend fun record(entry: DownloadHistory)

    suspend fun clear()
}
