package com.hdownloader.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdownloader.core.database.entity.DownloadEntity
import com.hdownloader.core.download.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(download: DownloadEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(downloads: List<DownloadEntity>)

    @Update
    suspend fun update(download: DownloadEntity)

    @Delete
    suspend fun delete(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY createdAt DESC")
    fun observeByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    fun observeById(id: Long): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: Long): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE status IN (:statuses) ORDER BY createdAt ASC")
    suspend fun getByStatuses(statuses: List<DownloadStatus>): List<DownloadEntity>

    @Query("SELECT * FROM downloads ORDER BY createdAt ASC")
    suspend fun getAll(): List<DownloadEntity>

    @Query("SELECT COUNT(*) FROM downloads WHERE status = :status")
    fun observeCountByStatus(status: DownloadStatus): Flow<Int>

    @Query("UPDATE downloads SET status = :status, isPaused = :isPaused, errorMessage = NULL WHERE id = :id")
    suspend fun updateStatus(id: Long, status: DownloadStatus, isPaused: Boolean)

    @Query(
        "UPDATE downloads SET downloadedBytes = :downloadedBytes, speed = :speed, eta = :eta WHERE id = :id",
    )
    suspend fun updateProgress(id: Long, downloadedBytes: Long, speed: Long, eta: Long)

    @Query(
        """
        UPDATE downloads SET
            finalUrl = :finalUrl,
            totalBytes = :totalBytes,
            supportsRange = :supportsRange,
            connectionCount = :connectionCount,
            filePath = :filePath,
            mimeType = :mimeType
        WHERE id = :id
        """,
    )
    suspend fun updateMeta(
        id: Long,
        finalUrl: String?,
        totalBytes: Long,
        supportsRange: Boolean,
        connectionCount: Int,
        filePath: String?,
        mimeType: String?,
    )

    @Query("UPDATE downloads SET errorMessage = :message WHERE id = :id")
    suspend fun updateError(id: Long, message: String)

    @Query("UPDATE downloads SET retryCount = :count WHERE id = :id")
    suspend fun updateRetryCount(id: Long, count: Int)

    @Query(
        "UPDATE downloads SET downloadedBytes = 0, speed = 0, eta = 0, startedAt = :startedAt WHERE id = :id",
    )
    suspend fun resetProgress(id: Long, startedAt: Long)

    @Query("UPDATE downloads SET startedAt = :startedAt WHERE id = :id")
    suspend fun updateStartedAt(id: Long, startedAt: Long)

    @Query("UPDATE downloads SET filePath = :filePath, completedAt = :completedAt WHERE id = :id")
    suspend fun updateCompleted(id: Long, filePath: String?, completedAt: Long)

    @Query("DELETE FROM downloads")
    suspend fun clearAll()
}
