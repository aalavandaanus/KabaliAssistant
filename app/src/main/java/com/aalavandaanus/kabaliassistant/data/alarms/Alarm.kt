package com.aalavandaanus.kabaliassistant.data.alarms

data class Alarm(
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDays: Set<Int>,
    val vibrationEnabled: Boolean = true,
    val enabled: Boolean = true
)