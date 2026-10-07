package com.aalavandaanus.kabaliassistant.features.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmViewModel(
    private val alarmRepository: AlarmRepository,
    private val alarmService: AlarmService
) : ViewModel() {

    val alarms: StateFlow<List<Alarm>> =
        alarmRepository
            .getAllAlarms()
            .stateIn(
                scope = viewModelScope,
                started =
                    SharingStarted.WhileSubscribed(
                        5_000L
                    ),
                initialValue = emptyList()
            )

    fun createAlarm(
        alarm: Alarm,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {

            val result =
                alarmService.create(
                    alarm
                )

            onResult?.invoke(result)
        }
    }

    fun updateAlarm(
        alarm: Alarm,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {

            val result =
                alarmService.update(
                    alarm
                )

            onResult?.invoke(result)
        }
    }

    fun setAlarmEnabled(
        alarm: Alarm,
        enabled: Boolean,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {

            val result =
                alarmService.setEnabled(
                    alarm = alarm,
                    enabled = enabled
                )

            onResult?.invoke(result)
        }
    }

    fun deleteAlarm(
        alarm: Alarm,
        onResult: (() -> Unit)? = null
    ) {
        viewModelScope.launch {

            alarmService.delete(
                alarm
            )

            onResult?.invoke()
        }
    }
}
