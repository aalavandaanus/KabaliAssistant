package com.aalavandaanus.kabaliassistant.features.reminders

import android.content.Context
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationCategory
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationChannelType
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationPriority
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationRequest
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventHandler
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventResult
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventType
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderRepository

class ReminderScheduledEventHandler(
    private val reminderRepository: ReminderRepository
) : ScheduledEventHandler {

    override val eventType = ScheduledEventType.REMINDER

    override suspend fun handle(
        context: Context,
        eventId: Long
    ): ScheduledEventResult {

        val reminder = reminderRepository.getReminderById(eventId)
            ?: return ScheduledEventResult(notification = null)

        if (!reminder.enabled) {
            return ScheduledEventResult(notification = null)
        }

        val notification = NotificationRequest(
            id = reminder.id,
            title = reminder.title,
            message = reminder.message,
            channel = NotificationChannelType.GENERAL,
            priority = NotificationPriority.HIGH,
            category = NotificationCategory.REMINDER,
            autoCancel = true,
            ongoing = false
        )

        return ScheduledEventResult(
            notification = notification,
            nextEvent = null
        )
    }
}