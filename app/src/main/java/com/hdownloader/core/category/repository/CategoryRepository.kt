package com.hdownloader.core.category.repository

import com.hdownloader.core.category.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun observeAll(): Flow<List<Category>>

    suspend fun getAll(): List<Category>

    suspend fun create(name: String): Long

    /**
     * Inserts the built-in categories when none exist yet.
     */
    suspend fun ensureDefaultCategories()
}
