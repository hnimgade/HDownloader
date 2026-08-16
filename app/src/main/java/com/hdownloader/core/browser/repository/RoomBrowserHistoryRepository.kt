package com.hdownloader.core.browser.repository

import com.hdownloader.core.browser.model.BrowserHistoryEntry
import com.hdownloader.core.database.dao.BrowserHistoryDao
import com.hdownloader.core.database.entity.BrowserHistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBrowserHistoryRepository @Inject constructor(
    private val historyDao: BrowserHistoryDao,
) : BrowserHistoryRepository {

    override fun observeAll(): Flow<List<BrowserHistoryEntry>> =
        historyDao.observeAll().map { list -> list.map(BrowserHistoryEntity::toModel) }

    override suspend fun record(url: String, title: String?) {
        historyDao.insert(BrowserHistoryEntity(url = url, title = title))
    }

    override suspend fun clear() {
        historyDao.clearAll()
    }
}

private fun BrowserHistoryEntity.toModel(): BrowserHistoryEntry = BrowserHistoryEntry(
    id = id,
    url = url,
    title = title,
    visitedAt = visitedAt,
)
