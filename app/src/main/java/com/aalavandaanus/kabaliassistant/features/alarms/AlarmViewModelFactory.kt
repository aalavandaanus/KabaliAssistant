package com.aalavandaanus.kabaliassistant.features.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository

class AlarmViewModelFactory(
    private val alarmRepository: AlarmRepository,
    private val alarmService: AlarmService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(AlarmViewModel::class.java)) {
            return AlarmViewModel(
                alarmRepository = alarmRepository,
                alarmService = alarmService
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
