
package com.aalavandaanus.kabaliassistant.features.schedules

import android.content.Context
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationCategory
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationChannelType
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationPriority
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationRequest
import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduleCalculator
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEvent
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventHandler
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventResult
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventType
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleRepository

class ScheduleScheduledEventHandler(
    private val scheduleRepository: ScheduleRepository
) : ScheduledEventHandler {

    override val eventType = ScheduledEventType.SCHEDULE

    override suspend fun handle(
        context: Context,
        eventId: Long
    ): ScheduledEventResult {
        val schedule = scheduleRepository.getScheduleById(eventId)
            ?: return ScheduledEventResult(notification = null)

        if (!schedule.enabled || schedule.completed) {
            return ScheduledEventResult(notification = null)
        }

        val notification = NotificationRequest(
            id = schedule.id,
            title = schedule.title,
            message = schedule.message,
            channel = NotificationChannelType.GENERAL,
            priority = NotificationPriority.HIGH,
            category = NotificationCategory.REMINDER,
            autoCancel = true,
            ongoing = false
        )

        val nextEvent = if (schedule.recurrence != Recurrence.None) {
            val nextTrigger = ScheduleCalculator.calculateNextTrigger(
                triggerAtMillis = schedule.triggerAtMillis,
                recurrence = schedule.recurrence,
                nowMillis = System.currentTimeMillis()
            )

            // Persist the next occurrence so the Schedules screen
            // displays the correct upcoming date and time.
            scheduleRepository.updateSchedule(
                schedule.copy(triggerAtMillis = nextTrigger)
            )

            ScheduledEvent(
                id = schedule.id,
                type = ScheduledEventType.SCHEDULE,
                triggerAtMillis = nextTrigger,
                enabled = true,
                recurrence = schedule.recurrence
            )
        } else {
            null
        }

        return ScheduledEventResult(
            notification = notification,
            nextEvent = nextEvent
        )
    }
}