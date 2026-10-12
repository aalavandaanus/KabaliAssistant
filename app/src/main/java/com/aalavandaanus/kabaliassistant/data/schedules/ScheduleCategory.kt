package com.aalavandaanus.kabaliassistant.data.schedules

data class ScheduleCategory(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val color: Long,
    val isBuiltIn: Boolean = false,
    val sortOrder: Int = 100
)