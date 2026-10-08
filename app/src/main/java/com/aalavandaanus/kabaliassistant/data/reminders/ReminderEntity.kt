package com.aalavandaanus.kabaliassistant.data.reminders

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val triggerAtMillis: Long,
    val enabled: Boolean = true
)