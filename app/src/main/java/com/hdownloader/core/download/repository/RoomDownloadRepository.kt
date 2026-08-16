package com.hdownloader.core.download.repository

import com.hdownloader.core.database.dao.DownloadDao
import com.hdownloader.core.database.entity.DownloadEntity
import com.hdownloader.core.download.engine.DownloadStateMachine
import com.hdownloader.core.download.model.DownloadState
import com.hdownloader.core.download.model.DownloadStatus
import com.hdownloader.core.network.UrlParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed [DownloadRepository]. Persists download state and enforces the
 * state machine on every mutation. Network transfer itself is delegated to the
 * DownloadEngine in a later phase.
 */
@Singleton
class RoomDownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao,
) : DownloadRepository {

    override fun observeAll(): Flow<List<DownloadState>> =
        downloadDao.observeAll().map { list -> list.map(DownloadEntity::toState) }

    override fun observeByStatus(status: DownloadStatus): Flow<List<DownloadState>> =
        downloadDao.observeByStatus(status).map { list -> list.map(DownloadEntity::toState) }

    override fun observeById(id: Long): Flow<DownloadState?> =
        downloadDao.observeById(id).map { it?.toState() }

    override suspend fun addDownload(
        url: String,
        fileName: String,
        categoryId: Long?,
        startImmediately: Boolean,
    ): Long {
        val parsed = UrlParser.parse(url)
        val status = if (startImmediately) DownloadStatus.QUEUED else DownloadStatus.PAUSED
        val entity = DownloadEntity(
            url = parsed.cleanUrl,
            fileName = fileName.ifBlank { parsed.fileName },
            extension = parsed.extension,
            status = status,
            categoryId = categoryId,
            supportsRange = false,
            connectionCount = 1,
        )
        return downloadDao.upsert(entity)
    }

    override suspend fun pause(id: Long) {
        transition(id, DownloadStatus.PAUSED)
    }

    override suspend fun resume(id: Long) {
        transition(id, DownloadStatus.QUEUED)
    }

    override suspend fun cancel(id: Long) {
        transition(id, DownloadStatus.CANCELLED)
    }

    override suspend fun retry(id: Long) {
        transition(id, DownloadStatus.QUEUED)
    }

    override suspend fun remove(id: Long) {
        downloadDao.deleteById(id)
    }

    override suspend fun clearCompleted() {
        downloadDao.getByStatuses(listOf(DownloadStatus.COMPLETED, DownloadStatus.CANCELLED))
            .forEach { downloadDao.delete(it) }
    }

    private suspend fun transition(id: Long, target: DownloadStatus) {
        val entity = downloadDao.getById(id) ?: return
        DownloadStateMachine.transition(entity.status, target)
        downloadDao.updateStatus(
            id = id,
            status = target,
            isPaused = target == DownloadStatus.PAUSED,
        )
    }
}

private fun DownloadEntity.toState(): DownloadState = DownloadState(
    id = id,
    url = url,
    finalUrl = finalUrl,
    fileName = fileName,
    status = status,
    totalBytes = totalBytes,
    downloadedBytes = downloadedBytes,
    speed = speed,
    eta = eta,
    supportsRange = supportsRange,
    connectionCount = connectionCount,
    errorMessage = errorMessage,
)
