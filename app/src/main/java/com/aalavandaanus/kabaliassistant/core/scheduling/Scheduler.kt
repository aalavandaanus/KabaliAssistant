
package com.aalavandaanus.kabaliassistant.core.scheduling

interface Scheduler {

    fun schedule(
        request: ScheduleRequest
    ): Boolean

    fun cancel(
        eventType: ScheduledEventType,
        eventId: Long
    )
}