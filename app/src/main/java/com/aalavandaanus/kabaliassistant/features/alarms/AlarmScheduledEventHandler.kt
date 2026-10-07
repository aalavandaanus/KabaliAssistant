package com.aalavandaanus.kabaliassistant.features.alarms

import android.content.Context
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationCategory
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationChannelType
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationPriority
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationRequest
import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEvent
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventHandler
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventResult
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventType
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository
import java.util.Calendar

class AlarmScheduledEventHandler(
    private val alarmRepository: AlarmRepository
) : ScheduledEventHandler {

    override val eventType =
        ScheduledEventType.ALARM

    override suspend fun handle(
        context: Context,
        eventId: Long
    ): ScheduledEventResult {

        val alarm =
            alarmRepository.getAlarmById(eventId)
                ?: return ScheduledEventResult(
                    notification = null
                )

        if (!alarm.enabled) {
            return ScheduledEventResult(
                notification = null
            )
        }

        val notification =
            createNotification(alarm)

        val nextEvent =
            createNextEvent(alarm)

        return ScheduledEventResult(
            notification = notification,
            nextEvent = nextEvent
        )
    }

    private fun createNotification(
        alarm: Alarm
    ) =
        NotificationRequest(
            id = alarm.id,
            title = alarm.label.ifBlank {
                "Alarm"
            },
            message = "It's time.",
            channel = NotificationChannelType.ALARM,
            priority = NotificationPriority.MAX,
            category = NotificationCategory.ALARM,
            autoCancel = true,
            ongoing = false
        )

    private fun createNextEvent(
        alarm: Alarm
    ): ScheduledEvent? {

        if (alarm.repeatDays.isEmpty()) {
            return null
        }

        val triggerAtMillis =
            createTriggerTime(alarm)

        return ScheduledEvent(
            id = alarm.id,
            type = ScheduledEventType.ALARM,
            triggerAtMillis = triggerAtMillis,
            enabled = alarm.enabled,
            recurrence = Recurrence.Weekly(
                daysOfWeek = alarm.repeatDays
            )
        )
    }

    private fun createTriggerTime(
        alarm: Alarm
    ): Long {

        return Calendar.getInstance().apply {
            set(
                Calendar.HOUR_OF_DAY,
                alarm.hour
            )
            set(
                Calendar.MINUTE,
                alarm.minute
            )
            set(
                Calendar.SECOND,
                0
            )
            set(
                Calendar.MILLISECOND,
                0
            )
        }.timeInMillis
    }
}