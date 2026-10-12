
package com.aalavandaanus.kabaliassistant.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderRepository
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategoryRepository
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleRepository

class HomeViewModelFactory(
    private val alarmRepository: AlarmRepository,
    private val reminderRepository: ReminderRepository,
    private val birthdayRepository: BirthdayRepository,
    private val scheduleRepository: ScheduleRepository,
    private val scheduleCategoryRepository: ScheduleCategoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                alarmRepository = alarmRepository,
                reminderRepository = reminderRepository,
                birthdayRepository = birthdayRepository,
                scheduleRepository = scheduleRepository,
                categoryRepository = scheduleCategoryRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}