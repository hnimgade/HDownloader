package com.hdownloader.core.category.repository

import com.hdownloader.core.category.model.Category
import com.hdownloader.core.database.dao.CategoryDao
import com.hdownloader.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override fun observeAll(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map(CategoryEntity::toModel) }

    override suspend fun getAll(): List<Category> =
        categoryDao.getAll().map(CategoryEntity::toModel)

    override suspend fun create(name: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return 0L
        return categoryDao.insert(CategoryEntity(name = trimmed))
    }

    override suspend fun ensureDefaultCategories() {
        if (categoryDao.getAll().isEmpty()) {
            categoryDao.insertAll(DEFAULT_CATEGORIES.map { CategoryEntity(name = it) })
        }
    }

    companion object {
        val DEFAULT_CATEGORIES = listOf("Videos", "Music", "Images", "Documents", "Other")
    }
}

private fun CategoryEntity.toModel(): Category = Category(id = id, name = name)
