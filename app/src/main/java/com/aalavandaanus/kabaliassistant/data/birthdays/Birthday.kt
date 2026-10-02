package com.aalavandaanus.kabaliassistant.data.birthdays

data class Birthday(
    val name: String,
    val relation: String,
    val contactNumber: String,
    val month: Int,
    val day: Int,
    val year: Int,
    val notificationEnabled: Boolean = true,
    val notificationDaysBefore: Int = 1
)