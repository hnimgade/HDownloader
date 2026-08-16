package com.hdownloader.core.bookmark.repository

import com.hdownloader.core.bookmark.model.Bookmark
import kotlinx.coroutines.flow.Flow

interface BookmarkRepository {

    fun observeAll(): Flow<List<Bookmark>>

    suspend fun add(url: String, title: String?)

    suspend fun remove(id: Long)

    suspend fun isBookmarked(url: String): Boolean
}
