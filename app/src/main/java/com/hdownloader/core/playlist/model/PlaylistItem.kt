package com.hdownloader.core.playlist.model

data class PlaylistItem(
    val id: Long = 0L,
    val playlistId: Long = 0L,
    val mediaId: Long? = null,
    val title: String,
    val filePath: String? = null,
    val position: Int = 0,
    val addedAt: Long = System.currentTimeMillis(),
)

data class NewPlaylistItem(
    val mediaId: Long? = null,
    val title: String,
    val filePath: String? = null,
)
