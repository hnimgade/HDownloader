package com.hdownloader.core.history.repository

import com.hdownloader.core.database.dao.DownloadHistoryDao
import com.hdownloader.core.database.entity.DownloadHistoryEntity
import com.hdownloader.core.history.model.DownloadHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomDownloadHistoryRepository @Inject constructor(
    private val historyDao: DownloadHistoryDao,
) : DownloadHistoryRepository {

    override fun observeAll(): Flow<List<DownloadHistory>> =
        historyDao.observeAll().map { list -> list.map(DownloadHistoryEntity::toModel) }

    override suspend fun record(entry: DownloadHistory) {
        historyDao.insert(entry.toEntity())
    }

    override suspend fun clear() {
        historyDao.clearAll()
    }
}

private fun DownloadHistoryEntity.toModel(): DownloadHistory = DownloadHistory(
    id = id,
    url = url,
    fileName = fileName,
    mimeType = mimeType,
    size = size,
    sourceHost = sourceHost,
    status = status,
    downloadedAt = downloadedAt,
)

private fun DownloadHistory.toEntity(): DownloadHistoryEntity = DownloadHistoryEntity(
    url = url,
    fileName = fileName,
    mimeType = mimeType,
    size = size,
    sourceHost = sourceHost,
    status = status,
    downloadedAt = downloadedAt,
)
