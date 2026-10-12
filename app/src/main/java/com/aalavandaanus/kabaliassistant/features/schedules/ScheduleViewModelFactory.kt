
package com.aalavandaanus.kabaliassistant.features.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategoryRepository
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleRepository

class ScheduleViewModelFactory(
    private val scheduleRepository: ScheduleRepository,
    private val categoryRepository: ScheduleCategoryRepository,
    private val scheduleService: ScheduleService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
            return ScheduleViewModel(
                scheduleRepository = scheduleRepository,
                categoryRepository = categoryRepository,
                scheduleService = scheduleService
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}