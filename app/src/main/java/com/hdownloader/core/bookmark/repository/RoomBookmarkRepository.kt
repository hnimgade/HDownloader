package com.hdownloader.core.bookmark.repository

import com.hdownloader.core.bookmark.model.Bookmark
import com.hdownloader.core.database.dao.BookmarkDao
import com.hdownloader.core.database.entity.BookmarkEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomBookmarkRepository @Inject constructor(
    private val bookmarkDao: BookmarkDao,
) : BookmarkRepository {

    override fun observeAll(): Flow<List<Bookmark>> =
        bookmarkDao.observeAll().map { list -> list.map(BookmarkEntity::toModel) }

    override suspend fun add(url: String, title: String?) {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return
        if (bookmarkDao.getByUrl(trimmed) == null) {
            bookmarkDao.insert(BookmarkEntity(url = trimmed, title = title))
        }
    }

    override suspend fun remove(id: Long) {
        bookmarkDao.delete(BookmarkEntity(id = id, url = ""))
    }

    override suspend fun isBookmarked(url: String): Boolean =
        bookmarkDao.getByUrl(url.trim()) != null
}

private fun BookmarkEntity.toModel(): Bookmark = Bookmark(
    id = id,
    url = url,
    title = title,
    createdAt = createdAt,
)
