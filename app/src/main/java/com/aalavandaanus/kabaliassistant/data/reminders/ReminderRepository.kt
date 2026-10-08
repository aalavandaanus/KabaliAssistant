package com.aalavandaanus.kabaliassistant.data.reminders

import kotlinx.coroutines.flow.Flow

interface ReminderRepository {

    fun getAllReminders(): Flow<List<Reminder>>

    suspend fun getReminderById(id: Long): Reminder?

    suspend fun insertReminder(reminder: Reminder): Long

    suspend fun updateReminder(reminder: Reminder)

    suspend fun deleteReminder(reminder: Reminder)
}