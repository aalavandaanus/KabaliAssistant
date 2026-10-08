package com.aalavandaanus.kabaliassistant.data.birthdays

data class Birthday(
    val id: Long = 0,
    val name: String,
    val relation: String,
    val contactNumber: String,
    val month: Int,
    val day: Int,
    val birthYear: Int? = null,
    val reminderEnabled: Boolean = true
)