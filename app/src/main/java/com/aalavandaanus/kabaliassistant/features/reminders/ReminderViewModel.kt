package com.aalavandaanus.kabaliassistant.features.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalavandaanus.kabaliassistant.data.reminders.Reminder
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReminderViewModel(
    private val reminderRepository: ReminderRepository,
    private val reminderService: ReminderService
) : ViewModel() {

    val reminders: StateFlow<List<Reminder>> =
        reminderRepository
            .getAllReminders()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = emptyList()
            )

    fun createReminder(
        reminder: Reminder,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = reminderService.create(reminder)
            onResult?.invoke(result)
        }
    }

    fun updateReminder(
        reminder: Reminder,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = reminderService.update(reminder)
            onResult?.invoke(result)
        }
    }

    fun setReminderEnabled(
        reminder: Reminder,
        enabled: Boolean,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = reminderService.setEnabled(
                reminder = reminder,
                enabled = enabled
            )
            onResult?.invoke(result)
        }
    }

    fun deleteReminder(
        reminder: Reminder,
        onResult: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            reminderService.delete(reminder)
            onResult?.invoke()
        }
    }
}