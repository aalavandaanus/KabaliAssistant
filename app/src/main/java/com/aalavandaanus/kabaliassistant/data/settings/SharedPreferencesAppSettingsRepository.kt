package com.aalavandaanus.kabaliassistant.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val _assistantName = MutableStateFlow(
        preferences.getString(
            KEY_ASSISTANT_NAME,
            DEFAULT_ASSISTANT_NAME
        ) ?: DEFAULT_ASSISTANT_NAME
    )

    override val assistantName: StateFlow<String> =
        _assistantName.asStateFlow()
    override fun getAssistantName(): String =
        _assistantName.value

    override fun setAssistantName(name: String) {
        val savedName = name.trim()
        if (savedName.isEmpty()) return

        preferences.edit()
            .putString(KEY_ASSISTANT_NAME, savedName)
            .apply()

        _assistantName.value = savedName
    }

    companion object {
        private const val PREFS_NAME = "kabali_assistant_settings"
        private const val KEY_ASSISTANT_NAME = "assistant_name"
        private const val DEFAULT_ASSISTANT_NAME = "Kabali"
        private const val KEY_BIRTHDAY_NOTIFICATION_HOUR =
            "birthday_notification_hour"

        private const val KEY_BIRTHDAY_NOTIFICATION_MINUTE =
            "birthday_notification_minute"

        private const val DEFAULT_HOUR = 9
        private const val DEFAULT_MINUTE = 0
    }
}