package com.hdownloader.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hdownloader.core.database.entity.BrowserHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowserHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BrowserHistoryEntity)

    @Delete
    suspend fun delete(entry: BrowserHistoryEntity)

    @Query("SELECT * FROM browser_history ORDER BY visitedAt DESC")
    fun observeAll(): Flow<List<BrowserHistoryEntity>>

    @Query("DELETE FROM browser_history")
    suspend fun clearAll()
}
