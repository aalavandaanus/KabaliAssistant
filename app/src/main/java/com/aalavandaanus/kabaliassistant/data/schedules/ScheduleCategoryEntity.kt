
package com.aalavandaanus.kabaliassistant.data.schedules

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_categories",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class ScheduleCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Long,
    val isBuiltIn: Boolean,
    val sortOrder: Int
)