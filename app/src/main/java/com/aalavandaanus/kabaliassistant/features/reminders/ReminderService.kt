package com.aalavandaanus.kabaliassistant.features.reminders

import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduleRequest
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEvent
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventType
import com.aalavandaanus.kabaliassistant.core.scheduling.Scheduler
import com.aalavandaanus.kabaliassistant.data.reminders.Reminder
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderRepository

class ReminderService(
    private val reminderRepository: ReminderRepository,
    private val scheduler: Scheduler
) {

    suspend fun create(reminder: Reminder): Boolean {

        if (reminder.enabled &&
            reminder.triggerAtMillis <= System.currentTimeMillis()
        ) {
            return false
        }

        val id = reminderRepository.insertReminder(reminder)

        val savedReminder = reminder.copy(id = id)

        if (!savedReminder.enabled) {
            return true
        }

        val scheduled = scheduler.schedule(
            ScheduleRequest(
                event = savedReminder.toScheduledEvent()
            )
        )

        if (!scheduled) {
            reminderRepository.deleteReminder(savedReminder)
        }

        return scheduled
    }

    suspend fun update(reminder: Reminder): Boolean {

        scheduler.cancel(reminder.id)

        reminderRepository.updateReminder(reminder)

        if (!reminder.enabled) {
            return true
        }

        if (reminder.triggerAtMillis <= System.currentTimeMillis()) {
            return false
        }

        return scheduler.schedule(
            ScheduleRequest(
                event = reminder.toScheduledEvent()
            )
        )
    }

    suspend fun setEnabled(
        reminder: Reminder,
        enabled: Boolean
    ): Boolean {

        val updatedReminder = reminder.copy(
            enabled = enabled
        )

        if (!enabled) {
            reminderRepository.updateReminder(updatedReminder)
            scheduler.cancel(reminder.id)
            return true
        }

        if (updatedReminder.triggerAtMillis <= System.currentTimeMillis()) {
            return false
        }

        reminderRepository.updateReminder(updatedReminder)

        return scheduler.schedule(
            ScheduleRequest(
                event = updatedReminder.toScheduledEvent()
            )
        )
    }



    suspend fun markDone(reminder: Reminder): Boolean {
        if (reminder.completedAtMillis != null) {
            return true
        }

        scheduler.cancel(reminder.id)

        val completedReminder = reminder.copy(
            completedAtMillis = System.currentTimeMillis()
        )

        reminderRepository.updateReminder(completedReminder)

        val savedReminder = reminderRepository.getReminderById(reminder.id)

        android.util.Log.d(
            "ReminderDebug",
            "Done tapped: id=${reminder.id}, " +
                    "savedCompletedAt=${savedReminder?.completedAtMillis}"
        )

        return savedReminder?.completedAtMillis != null
    }



    suspend fun undoCompletion(reminder: Reminder): Boolean {
        if (reminder.completedAtMillis == null) {
            return true
        }

        val restoredReminder = reminder.copy(
            completedAtMillis = null
        )

        // A future reminder needs its notification scheduled again.
        // A past reminder remains overdue without another notification.
        if (
            restoredReminder.enabled &&
            restoredReminder.triggerAtMillis > System.currentTimeMillis()
        ) {
            val scheduled = scheduler.schedule(
                ScheduleRequest(
                    event = restoredReminder.toScheduledEvent()
                )
            )

            if (!scheduled) {
                return false
            }
        }

        reminderRepository.updateReminder(restoredReminder)
        return true
    }

    suspend fun delete(reminder: Reminder) {
        scheduler.cancel(reminder.id)
        reminderRepository.deleteReminder(reminder)
    }

    private fun Reminder.toScheduledEvent(): ScheduledEvent {
        return ScheduledEvent(
            id = id,
            type = ScheduledEventType.REMINDER,
            triggerAtMillis = triggerAtMillis,
            enabled = enabled,
            recurrence = Recurrence.None
        )
    }
}