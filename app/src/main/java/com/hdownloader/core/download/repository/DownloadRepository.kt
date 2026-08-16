package com.hdownloader.core.download.repository

import com.hdownloader.core.download.model.DownloadState
import com.hdownloader.core.download.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

/**
 * Contract between the UI layer and the download subsystem.
 *
 * The concrete implementation is backed by Room; actual network transfer is
 * handled by the DownloadEngine (Phase 3). This interface deliberately exposes
 * domain concepts rather than database entities.
 */
interface DownloadRepository {

    fun observeAll(): Flow<List<DownloadState>>

    fun observeByStatus(status: DownloadStatus): Flow<List<DownloadState>>

    fun observeById(id: Long): Flow<DownloadState?>

    suspend fun addDownload(
        url: String,
        fileName: String,
        categoryId: Long? = null,
        startImmediately: Boolean = true,
    ): Long

    suspend fun pause(id: Long)

    suspend fun resume(id: Long)

    suspend fun cancel(id: Long)

    suspend fun retry(id: Long)

    suspend fun remove(id: Long)

    suspend fun clearCompleted()
}
