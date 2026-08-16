package com.hdownloader.core.playlist.repository

import com.hdownloader.core.database.dao.PlaylistDao
import com.hdownloader.core.database.entity.PlaylistEntity
import com.hdownloader.core.database.entity.PlaylistItemEntity
import com.hdownloader.core.playlist.model.NewPlaylistItem
import com.hdownloader.core.playlist.model.Playlist
import com.hdownloader.core.playlist.model.PlaylistItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomPlaylistRepository @Inject constructor(
    private val playlistDao: PlaylistDao,
) : PlaylistRepository {

    override fun observePlaylists(): Flow<List<Playlist>> =
        playlistDao.observeAll().map { list -> list.map(PlaylistEntity::toModel) }

    override fun observeItems(playlistId: Long): Flow<List<PlaylistItem>> =
        playlistDao.observeItems(playlistId).map { list -> list.map(PlaylistItemEntity::toModel) }

    override suspend fun create(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return 0L
        return playlistDao.insert(PlaylistEntity(name = trimmed))
    }

    override suspend fun rename(id: Long, name: String) {
        val existing = playlistDao.getById(id) ?: return
        playlistDao.update(existing.copy(name = name.trim(), updatedAt = System.currentTimeMillis()))
    }

    override suspend fun delete(id: Long) {
        val existing = playlistDao.getById(id) ?: return
        playlistDao.delete(existing)
    }

    override suspend fun addItems(playlistId: Long, items: List<NewPlaylistItem>) {
        if (items.isEmpty()) return
        val existing = playlistDao.getById(playlistId) ?: return
        val currentItems = playlistDao.observeItems(playlistId).first()
        var position = currentItems.maxOfOrNull { it.position }?.plus(1) ?: 0
        val entities = items.map { item ->
            PlaylistItemEntity(
                playlistId = playlistId,
                mediaId = item.mediaId,
                title = item.title,
                filePath = item.filePath,
                position = position++,
            )
        }
        playlistDao.insertItems(entities)
        playlistDao.update(existing.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun removeItem(itemId: Long) {
        playlistDao.deleteItem(itemId)
    }

    override suspend fun reorder(playlistId: Long, orderedItemIds: List<Long>) {
        playlistDao.reorder(playlistId, orderedItemIds)
    }
}

private fun PlaylistEntity.toModel(): Playlist = Playlist(
    id = id,
    name = name,
    coverUri = coverUri,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun PlaylistItemEntity.toModel(): PlaylistItem = PlaylistItem(
    id = id,
    playlistId = playlistId,
    mediaId = mediaId,
    title = title,
    filePath = filePath,
    position = position,
    addedAt = addedAt,
)
