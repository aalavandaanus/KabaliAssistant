package com.aalavandaanus.kabaliassistant.core.scheduling

data class ScheduledEvent(
    val id: Long,
    val type: ScheduledEventType,
    val triggerAtMillis: Long,
    val enabled: Boolean = true,
    val recurrence: Recurrence = Recurrence.None
)