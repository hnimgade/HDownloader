package com.hdownloader.core.media.repository

import com.hdownloader.core.media.model.Media
import com.hdownloader.core.media.model.MediaCategory
import kotlinx.coroutines.flow.Flow

interface MediaRepository {

    fun observeAll(): Flow<List<Media>>

    fun observeByCategory(category: MediaCategory): Flow<List<Media>>

    suspend fun upsert(media: Media): Long

    suspend fun delete(mediaId: Long)
}
