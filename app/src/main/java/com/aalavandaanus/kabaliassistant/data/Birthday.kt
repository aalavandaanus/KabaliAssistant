package com.aalavandaanus.kabaliassistant.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val date: String,

    val relationship: String? = null,

    val notificationEnabled: Boolean = true,

    val notificationDaysBefore: Int = 1
)