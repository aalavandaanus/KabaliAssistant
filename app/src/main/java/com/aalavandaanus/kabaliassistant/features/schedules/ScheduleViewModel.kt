
package com.aalavandaanus.kabaliassistant.features.schedules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalavandaanus.kabaliassistant.data.schedules.Schedule
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategory
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategoryRepository
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val categoryRepository: ScheduleCategoryRepository,
    private val scheduleService: ScheduleService
) : ViewModel() {

    val schedules = scheduleRepository.getAllSchedules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val categories = categoryRepository.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun createSchedule(schedule: Schedule, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = runCatching {
                scheduleService.create(schedule)
            }.getOrDefault(false)

            onResult(success)
        }
    }

    fun updateSchedule(schedule: Schedule, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = runCatching {
                scheduleService.update(schedule)
            }.getOrDefault(false)

            onResult(success)
        }
    }

    fun setScheduleEnabled(
        schedule: Schedule,
        enabled: Boolean,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = runCatching {
                scheduleService.setEnabled(schedule, enabled)
            }.getOrDefault(false)

            onResult(success)
        }
    }

    fun markCompleted(schedule: Schedule, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = runCatching {
                scheduleService.markCompleted(schedule)
            }.getOrDefault(false)

            onResult(success)
        }
    }

    fun deleteSchedule(schedule: Schedule, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = runCatching {
                scheduleService.delete(schedule)
                true
            }.getOrDefault(false)

            onResult(success)
        }
    }

    fun createCategory(
        category: ScheduleCategory,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = runCatching {
                categoryRepository.insertCategory(category)
                true
            }.getOrDefault(false)

            onResult(success)
        }
    }

    fun updateCategory(
        category: ScheduleCategory,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = runCatching {
                categoryRepository.updateCategory(category)
                true
            }.getOrDefault(false)

            onResult(success)
        }
    }

    fun deleteCategory(
        category: ScheduleCategory,
        onResult: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = runCatching {
                categoryRepository.deleteCategory(category)
                true
            }.getOrDefault(false)

            onResult(success)
        }
    }
}