package com.hdownloader.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hdownloader.core.database.entity.DownloadPartEntity
import com.hdownloader.core.download.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadPartDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(parts: List<DownloadPartEntity>)

    @Update
    suspend fun update(part: DownloadPartEntity)

    @Query("SELECT * FROM download_parts WHERE downloadId = :downloadId ORDER BY partIndex ASC")
    fun observeParts(downloadId: Long): Flow<List<DownloadPartEntity>>

    @Query("SELECT * FROM download_parts WHERE downloadId = :downloadId ORDER BY partIndex ASC")
    suspend fun getParts(downloadId: Long): List<DownloadPartEntity>

    @Query("SELECT * FROM download_parts WHERE id = :id")
    suspend fun getPart(id: Long): DownloadPartEntity?

    @Query(
        "UPDATE download_parts SET currentByte = :currentByte, status = :status WHERE id = :id",
    )
    suspend fun updateProgress(id: Long, currentByte: Long, status: DownloadStatus)

    @Query("DELETE FROM download_parts WHERE downloadId = :downloadId")
    suspend fun deleteByDownloadId(downloadId: Long)
}
