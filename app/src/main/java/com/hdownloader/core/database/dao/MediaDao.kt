package com.hdownloader.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hdownloader.core.database.entity.MediaEntity
import com.hdownloader.core.media.model.MediaCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(media: MediaEntity): Long

    @Delete
    suspend fun delete(media: MediaEntity)

    @Query("SELECT * FROM media ORDER BY dateAdded DESC")
    fun observeAll(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media WHERE category = :category ORDER BY dateAdded DESC")
    fun observeByCategory(category: MediaCategory): Flow<List<MediaEntity>>

    @Query("DELETE FROM media WHERE id = :id")
    suspend fun deleteById(id: Long)
}
