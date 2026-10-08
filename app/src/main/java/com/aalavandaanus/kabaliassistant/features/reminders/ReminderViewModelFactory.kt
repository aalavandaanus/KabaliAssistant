package com.aalavandaanus.kabaliassistant.features.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderRepository

class ReminderViewModelFactory(
    private val reminderRepository: ReminderRepository,
    private val reminderService: ReminderService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(ReminderViewModel::class.java)) {
            return ReminderViewModel(
                reminderRepository = reminderRepository,
                reminderService = reminderService
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}