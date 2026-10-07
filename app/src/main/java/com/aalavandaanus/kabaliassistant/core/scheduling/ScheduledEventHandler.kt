package com.aalavandaanus.kabaliassistant.core.scheduling

import android.content.Context

interface ScheduledEventHandler {

    val eventType: ScheduledEventType

    suspend fun handle(
        context: Context,
        eventId: Long
    ): ScheduledEventResult
}