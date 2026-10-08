package com.aalavandaanus.kabaliassistant.data.settings

import android.content.Context

class SharedPreferencesAppSettingsRepository(
    context: Context
) : AppSettingsRepository {

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun getBirthdayNotificationHour(): Int {
        return preferences.getInt(
            KEY_BIRTHDAY_NOTIFICATION_HOUR,
            DEFAULT_HOUR
        )
    }

    override fun getBirthdayNotificationMinute(): Int {
        return preferences.getInt(
            KEY_BIRTHDAY_NOTIFICATION_MINUTE,
            DEFAULT_MINUTE
        )
    }

    override fun setBirthdayNotificationTime(
        hour: Int,
        minute: Int
    ) {
        preferences.edit()
            .putInt(KEY_BIRTHDAY_NOTIFICATION_HOUR, hour)
            .putInt(KEY_BIRTHDAY_NOTIFICATION_MINUTE, minute)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "kabali_assistant_settings"

        private const val KEY_BIRTHDAY_NOTIFICATION_HOUR =
            "birthday_notification_hour"

        private const val KEY_BIRTHDAY_NOTIFICATION_MINUTE =
            "birthday_notification_minute"

        private const val DEFAULT_HOUR = 9
        private const val DEFAULT_MINUTE = 0
    }
}