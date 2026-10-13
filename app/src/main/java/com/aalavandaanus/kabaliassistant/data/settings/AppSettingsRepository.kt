package com.aalavandaanus.kabaliassistant.data.settings

import kotlinx.coroutines.flow.StateFlow

interface AppSettingsRepository {
    fun getBirthdayNotificationHour(): Int
    fun getBirthdayNotificationMinute(): Int

    fun setBirthdayNotificationTime(
        hour: Int,
        minute: Int
    )

    fun getAssistantName(): String
    fun setAssistantName(name: String)

    val assistantName: StateFlow<String>
}