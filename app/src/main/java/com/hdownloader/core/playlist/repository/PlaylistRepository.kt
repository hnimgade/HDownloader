package com.hdownloader.core.playlist.repository

import com.hdownloader.core.playlist.model.NewPlaylistItem
import com.hdownloader.core.playlist.model.Playlist
import com.hdownloader.core.playlist.model.PlaylistItem
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {

    fun observePlaylists(): Flow<List<Playlist>>

    fun observeItems(playlistId: Long): Flow<List<PlaylistItem>>

    suspend fun create(name: String): Long

    suspend fun rename(id: Long, name: String)

    suspend fun delete(id: Long)

    suspend fun addItems(playlistId: Long, items: List<NewPlaylistItem>)

    suspend fun removeItem(itemId: Long)

    suspend fun reorder(playlistId: Long, orderedItemIds: List<Long>)
}
