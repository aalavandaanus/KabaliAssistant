package com.aalavandaanus.kabaliassistant.data.schedules

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomScheduleCategoryRepository(
    private val categoryDao: ScheduleCategoryDao
) : ScheduleCategoryRepository {

    override fun getAllCategories(): Flow<List<ScheduleCategory>> =
        categoryDao.getAllCategories().map { categories ->
            categories.map { it.toDomain() }
        }

    override suspend fun getCategoryById(id: Long): ScheduleCategory? =
        categoryDao.getCategoryById(id)?.toDomain()

    override suspend fun getCategoryByName(
        name: String
    ): ScheduleCategory? =
        categoryDao.getCategoryByName(name)?.toDomain()

    override suspend fun insertCategory(
        category: ScheduleCategory
    ): Long = categoryDao.insertCategory(category.toEntity())

    override suspend fun updateCategory(category: ScheduleCategory) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(category: ScheduleCategory) {
        require(!category.isBuiltIn) {
            "Built-in categories cannot be deleted."
        }
        categoryDao.deleteCategory(category.toEntity())
    }
}