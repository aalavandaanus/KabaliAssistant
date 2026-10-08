package com.aalavandaanus.kabaliassistant.data.settings

interface AppSettingsRepository {

    fun getBirthdayNotificationHour(): Int

    fun getBirthdayNotificationMinute(): Int

    fun setBirthdayNotificationTime(
        hour: Int,
        minute: Int
    )
}