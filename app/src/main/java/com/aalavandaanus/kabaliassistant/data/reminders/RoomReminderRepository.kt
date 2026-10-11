package com.aalavandaanus.kabaliassistant.data.reminders

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomReminderRepository(
    private val reminderDao: ReminderDao
) : ReminderRepository {

    override fun getAllReminders(): Flow<List<Reminder>> {
        return reminderDao.getAllReminders().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getReminderById(id: Long): Reminder? {
        return reminderDao.getReminderById(id)?.toDomain()
    }

    override suspend fun insertReminder(reminder: Reminder): Long {
        return reminderDao.insertReminder(reminder.toEntity())
    }

    override suspend fun updateReminder(reminder: Reminder) {
        reminderDao.updateReminder(reminder.toEntity())
    }

    override suspend fun deleteReminder(reminder: Reminder) {
        reminderDao.deleteReminder(reminder.toEntity())
    }


    private fun ReminderEntity.toDomain(): Reminder {
        return Reminder(
            id = id,
            title = title,
            message = message,
            triggerAtMillis = triggerAtMillis,
            enabled = enabled,
            completedAtMillis = completedAtMillis
        )
    }


    private fun Reminder.toEntity(): ReminderEntity {
        return ReminderEntity(
            id = id,
            title = title,
            message = message,
            triggerAtMillis = triggerAtMillis,
            enabled = enabled,
            completedAtMillis = completedAtMillis
        )
    }
}