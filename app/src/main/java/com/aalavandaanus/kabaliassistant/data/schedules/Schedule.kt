
package com.aalavandaanus.kabaliassistant.data.schedules

import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence

data class Schedule(
    val id: Long = 0,
    val title: String,
    val message: String,
    val categoryId: Long,
    val triggerAtMillis: Long,
    val recurrence: Recurrence = Recurrence.None,
    val enabled: Boolean = true,
    val completed: Boolean = false,
    val lastCompletedAtMillis: Long? = null
)