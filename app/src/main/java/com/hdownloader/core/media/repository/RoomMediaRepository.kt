package com.hdownloader.core.media.repository

import com.hdownloader.core.database.dao.MediaDao
import com.hdownloader.core.database.entity.MediaEntity
import com.hdownloader.core.media.model.Media
import com.hdownloader.core.media.model.MediaCategory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomMediaRepository @Inject constructor(
    private val mediaDao: MediaDao,
) : MediaRepository {

    override fun observeAll(): Flow<List<Media>> =
        mediaDao.observeAll().map { list -> list.map(MediaEntity::toModel) }

    override fun observeByCategory(category: MediaCategory): Flow<List<Media>> =
        mediaDao.observeByCategory(category).map { list -> list.map(MediaEntity::toModel) }

    override suspend fun upsert(media: Media): Long =
        mediaDao.upsert(media.toEntity())

    override suspend fun delete(mediaId: Long) {
        mediaDao.deleteById(mediaId)
    }
}

private fun MediaEntity.toModel(): Media = Media(
    id = id,
    fileName = fileName,
    mediaStoreUri = mediaStoreUri,
    filePath = filePath,
    mimeType = mimeType,
    size = size,
    duration = duration,
    category = category,
    dateAdded = dateAdded,
)

private fun Media.toEntity(): MediaEntity = MediaEntity(
    id = id,
    fileName = fileName,
    mediaStoreUri = mediaStoreUri,
    filePath = filePath,
    mimeType = mimeType,
    size = size,
    duration = duration,
    category = category,
    dateAdded = dateAdded,
)
