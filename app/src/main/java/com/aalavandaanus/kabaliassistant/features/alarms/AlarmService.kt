package com.aalavandaanus.kabaliassistant.features.alarms

import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduleRequest
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEvent
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventType
import com.aalavandaanus.kabaliassistant.core.scheduling.Scheduler
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository
import java.util.Calendar

class AlarmService(
    private val alarmRepository: AlarmRepository,
    private val scheduler: Scheduler
) {

    suspend fun create(alarm: Alarm): Boolean {
        val id = alarmRepository.insertAlarm(alarm)
        val savedAlarm = alarm.copy(id = id)

        if (!savedAlarm.enabled) {
            return true
        }

        val scheduled = scheduler.schedule(
            ScheduleRequest(
                event = savedAlarm.toScheduledEvent()
            )
        )

        if (!scheduled) {
            alarmRepository.deleteAlarm(savedAlarm)
        }

        return scheduled
    }

    suspend fun update(alarm: Alarm): Boolean {
        scheduler.cancel(alarm.id)

        alarmRepository.updateAlarm(alarm)

        if (!alarm.enabled) {
            return true
        }

        return scheduler.schedule(
            ScheduleRequest(
                event = alarm.toScheduledEvent()
            )
        )
    }

    suspend fun setEnabled(
        alarm: Alarm,
        enabled: Boolean
    ): Boolean {
        val updatedAlarm = alarm.copy(
            enabled = enabled
        )

        alarmRepository.updateAlarm(updatedAlarm)

        if (!enabled) {
            scheduler.cancel(alarm.id)
            return true
        }

        return scheduler.schedule(
            ScheduleRequest(
                event = updatedAlarm.toScheduledEvent()
            )
        )
    }

    suspend fun delete(alarm: Alarm) {
        scheduler.cancel(alarm.id)
        alarmRepository.deleteAlarm(alarm)
    }

    private fun Alarm.toScheduledEvent(): ScheduledEvent {
        val recurrence =
            if (repeatDays.isEmpty()) {
                Recurrence.None
            } else {
                Recurrence.Weekly(
                    daysOfWeek = repeatDays
                )
            }

        return ScheduledEvent(
            id = id,
            type = ScheduledEventType.ALARM,
            triggerAtMillis = createTriggerTime(recurrence),
            enabled = enabled,
            recurrence = recurrence
        )
    }

    private fun Alarm.createTriggerTime(
        recurrence: Recurrence
    ): Long {
        val now = Calendar.getInstance()

        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        /*
         * For a one-time alarm, the trigger must represent
         * an actual future occurrence.
         *
         * Example:
         * Current time = 10:00 AM
         * Alarm time   = 09:00 AM
         *
         * The alarm should therefore fire tomorrow at 09:00 AM.
         *
         * For recurring alarms, ScheduleCalculator is responsible
         * for finding the next occurrence, so we leave the trigger
         * on today's date.
         */
        if (recurrence == Recurrence.None &&
            trigger.timeInMillis <= now.timeInMillis
        ) {
            trigger.add(Calendar.DAY_OF_YEAR, 1)
        }

        return trigger.timeInMillis
    }
}
