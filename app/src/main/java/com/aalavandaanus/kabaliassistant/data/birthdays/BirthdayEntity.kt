package com.aalavandaanus.kabaliassistant.data.birthdays

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birthdays")
data class BirthdayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val relation: String,

    val contactNumber: String,

    val month: Int,

    val day: Int,

    val year: Int,

    val notificationEnabled: Boolean = true,

    val notificationDaysBefore: Int = 1
)