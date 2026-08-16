package com.hdownloader.core.playlist.model

data class Playlist(
    val id: Long = 0L,
    val name: String,
    val coverUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
