package com.aalavandaanus.kabaliassistant.data.birthdays

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "birthdays")
data class BirthdayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val relation: String,
    val contactNumber: String,
    val month: Int,
    val day: Int,
    val birthYear: Int? = null,
    val reminderEnabled: Boolean = true
)