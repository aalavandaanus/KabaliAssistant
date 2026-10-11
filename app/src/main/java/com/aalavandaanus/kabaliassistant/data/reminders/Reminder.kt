
package com.aalavandaanus.kabaliassistant.data.reminders

data class Reminder(
    val id: Long = 0,
    val title: String,
    val message: String,
    val triggerAtMillis: Long,
    val enabled: Boolean = true,
    val completedAtMillis: Long? = null
)