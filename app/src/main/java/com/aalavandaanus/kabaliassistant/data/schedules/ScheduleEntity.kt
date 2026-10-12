
package com.aalavandaanus.kabaliassistant.data.schedules

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["triggerAtMillis"])
    ]
)
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val categoryId: Long,
    val triggerAtMillis: Long,
    val recurrenceType: String = "NONE",
    val repeatDays: String = "",
    val enabled: Boolean = true,
    val completed: Boolean = false,
    val lastCompletedAtMillis: Long? = null
)