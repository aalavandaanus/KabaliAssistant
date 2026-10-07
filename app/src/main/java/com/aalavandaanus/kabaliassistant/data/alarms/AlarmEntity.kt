package com.aalavandaanus.kabaliassistant.data.alarms

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDays: String = "",
    val vibrationEnabled: Boolean = true,
    val enabled: Boolean = true
)