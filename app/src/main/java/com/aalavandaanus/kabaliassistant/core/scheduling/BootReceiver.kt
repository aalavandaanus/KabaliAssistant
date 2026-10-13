
package com.aalavandaanus.kabaliassistant.core.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aalavandaanus.kabaliassistant.KabaliApplication
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm
import com.aalavandaanus.kabaliassistant.data.reminders.Reminder
import com.aalavandaanus.kabaliassistant.data.schedules.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "KabaliBootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }

        val pendingResult = goAsync()
        val app = context.applicationContext as KabaliApplication
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                val alarms = app.alarmRepository.getAllAlarms().first()
                val reminders = app.reminderRepository.getAllReminders().first()
                val schedules = app.scheduleRepository.getAllSchedules().first()

                alarms.filter { it.enabled }.forEach { alarm ->
                    val event = alarm.toRestoredEvent()
                    logScheduleResult(
                        app.scheduler.schedule(ScheduleRequest(event)),
                        event
                    )
                }

                reminders
                    .filter {
                        it.enabled &&
                                it.completedAtMillis == null &&
                                it.triggerAtMillis > System.currentTimeMillis()
                    }
                    .forEach { reminder ->
                        val event = reminder.toRestoredEvent()
                        logScheduleResult(
                            app.scheduler.schedule(ScheduleRequest(event)),
                            event
                        )
                    }

                schedules
                    .filter { it.enabled && !it.completed }
                    .filter {
                        it.recurrence != Recurrence.None ||
                                it.triggerAtMillis > System.currentTimeMillis()
                    }
                    .forEach { schedule ->
                        val event = schedule.toRestoredEvent()
                        logScheduleResult(
                            app.scheduler.schedule(ScheduleRequest(event)),
                            event
                        )
                    }

                Log.i(TAG, "Finished restoring scheduled events.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore scheduled events.", e)
            } finally {
                pendingResult.finish()
                scope.cancel()
            }
        }
    }

    private fun Alarm.toRestoredEvent(): ScheduledEvent {
        val now = System.currentTimeMillis()
        val trigger = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (repeatDays.isEmpty() && timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val recurrence = if (repeatDays.isEmpty()) {
            Recurrence.None
        } else {
            Recurrence.Weekly(repeatDays)
        }

        return ScheduledEvent(
            id = id,
            type = ScheduledEventType.ALARM,
            triggerAtMillis = trigger.timeInMillis,
            enabled = enabled,
            recurrence = recurrence
        )
    }

    private fun Reminder.toRestoredEvent() = ScheduledEvent(
        id = id,
        type = ScheduledEventType.REMINDER,
        triggerAtMillis = triggerAtMillis,
        enabled = enabled,
        recurrence = Recurrence.None
    )

    private fun Schedule.toRestoredEvent() = ScheduledEvent(
        id = id,
        type = ScheduledEventType.SCHEDULE,
        triggerAtMillis = triggerAtMillis,
        enabled = enabled && !completed,
        recurrence = recurrence
    )

    private fun logScheduleResult(
        success: Boolean,
        event: ScheduledEvent
    ) {
        if (success) {
            Log.d(
                TAG,
                "Restored ${event.type} id=${event.id}"
            )
        } else {
            Log.w(
                TAG,
                "Could not restore ${event.type} id=${event.id}. " +
                        "Check exact-alarm permission and scheduling logs."
            )
        }
    }
}