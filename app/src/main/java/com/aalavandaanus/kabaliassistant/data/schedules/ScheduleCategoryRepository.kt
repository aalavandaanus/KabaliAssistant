package com.aalavandaanus.kabaliassistant.data.schedules

import kotlinx.coroutines.flow.Flow

interface ScheduleCategoryRepository {
    fun getAllCategories(): Flow<List<ScheduleCategory>>
    suspend fun getCategoryById(id: Long): ScheduleCategory?
    suspend fun getCategoryByName(name: String): ScheduleCategory?
    suspend fun insertCategory(category: ScheduleCategory): Long
    suspend fun updateCategory(category: ScheduleCategory)
    suspend fun deleteCategory(category: ScheduleCategory)
}