package com.aalavandaanus.kabaliassistant.core.scheduling

interface Scheduler {

    fun schedule(
        request: ScheduleRequest
    ): Boolean

    fun cancel(
        eventId: Long
    )
}