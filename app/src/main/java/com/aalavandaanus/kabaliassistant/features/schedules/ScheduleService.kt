
package com.aalavandaanus.kabaliassistant.features.schedules

import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduleCalculator
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduleRequest
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEvent
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventType
import com.aalavandaanus.kabaliassistant.core.scheduling.Scheduler
import com.aalavandaanus.kabaliassistant.data.schedules.Schedule
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleRepository

class ScheduleService(
    private val scheduleRepository: ScheduleRepository,
    private val scheduler: Scheduler
) {

    suspend fun create(schedule: Schedule): Boolean {
        val now = System.currentTimeMillis()
        val prepared = prepareForScheduling(schedule, now) ?: return false

        val id = scheduleRepository.insertSchedule(prepared)
        val saved = prepared.copy(id = id)

        if (!saved.enabled || saved.completed) return true

        if (scheduler.schedule(ScheduleRequest(saved.toEvent()))) {
            return true
        }

        scheduleRepository.deleteSchedule(saved)
        return false
    }

    suspend fun update(schedule: Schedule): Boolean {
        val now = System.currentTimeMillis()
        val prepared = prepareForScheduling(schedule, now) ?: return false

        scheduler.cancel(ScheduledEventType.SCHEDULE, prepared.id)
        scheduleRepository.updateSchedule(prepared)

        if (!prepared.enabled || prepared.completed) return true

        return scheduler.schedule(ScheduleRequest(prepared.toEvent()))
    }

    suspend fun setEnabled(
        schedule: Schedule,
        enabled: Boolean
    ): Boolean {
        if (!enabled) {
            scheduler.cancel(ScheduledEventType.SCHEDULE, schedule.id)
            scheduleRepository.updateSchedule(schedule.copy(enabled = false))
            return true
        }

        if (schedule.completed) return false

        val prepared = prepareForScheduling(
            schedule.copy(enabled = true),
            System.currentTimeMillis()
        ) ?: return false

        if (!scheduler.schedule(ScheduleRequest(prepared.toEvent()))) {
            return false
        }

        scheduleRepository.updateSchedule(prepared)
        return true
    }


    suspend fun markCompleted(schedule: Schedule): Boolean {
        val now = System.currentTimeMillis()

        return if (schedule.recurrence == Recurrence.None) {
            // A one-time schedule is finished permanently.
            scheduler.cancel(
                ScheduledEventType.SCHEDULE,
                schedule.id
            )

            scheduleRepository.updateSchedule(
                schedule.copy(
                    enabled = false,
                    completed = true,
                    lastCompletedAtMillis = now
                )
            )

            true
        } else {
            // The handler already advances and schedules the next occurrence.
            // Record completion without advancing it a second time.
            scheduleRepository.updateSchedule(
                schedule.copy(
                    lastCompletedAtMillis = now
                )
            )

            true
        }
    }

    suspend fun delete(schedule: Schedule) {
        scheduler.cancel(ScheduledEventType.SCHEDULE, schedule.id)
        scheduleRepository.deleteSchedule(schedule)
    }

    private fun prepareForScheduling(
        schedule: Schedule,
        now: Long
    ): Schedule? {
        if (!schedule.enabled || schedule.completed) return schedule

        if (schedule.triggerAtMillis > now) return schedule

        if (schedule.recurrence == Recurrence.None) return null

        return schedule.copy(
            triggerAtMillis = ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = schedule.triggerAtMillis,
                recurrence = schedule.recurrence,
                nowMillis = now
            )
        )
    }

    private fun Schedule.toEvent() = ScheduledEvent(
        id = id,
        type = ScheduledEventType.SCHEDULE,
        triggerAtMillis = triggerAtMillis,
        enabled = enabled && !completed,
        recurrence = recurrence
    )
}