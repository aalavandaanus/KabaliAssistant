package com.aalavandaanus.kabaliassistant.data.schedules

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleCategoryDao {

    @Query("SELECT * FROM schedule_categories ORDER BY sortOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<ScheduleCategoryEntity>>

    @Query("SELECT * FROM schedule_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): ScheduleCategoryEntity?

    @Query("SELECT * FROM schedule_categories WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getCategoryByName(name: String): ScheduleCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: ScheduleCategoryEntity): Long

    @Update
    suspend fun updateCategory(category: ScheduleCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: ScheduleCategoryEntity)
}

