package com.aalavandaanus.kabaliassistant.core.notifications

import android.app.NotificationChannel as AndroidNotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

object NotificationChannel {
    const val ALARM_CHANNEL_ID = "kabali_alarm_channel"
    const val GENERAL_CHANNEL_ID = "kabali_general_channel"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createAlarmChannel(notificationManager)
        createGeneralChannel(notificationManager)
    }

    fun getChannelId(channel: NotificationChannelType): String {
        return when (channel) {
            NotificationChannelType.ALARM -> ALARM_CHANNEL_ID
            NotificationChannelType.GENERAL
                -> GENERAL_CHANNEL_ID
        }
    }

    private fun createAlarmChannel(notificationManager: NotificationManager) {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        val channel = AndroidNotificationChannel(
            ALARM_CHANNEL_ID,
            "Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description =
                "Kabali Assistant alarm notifications"
            setSound(soundUri, audioAttributes)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 700, 400, 700, 400, 700)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun createGeneralChannel(notificationManager: NotificationManager) {
        val channel = AndroidNotificationChannel(
            GENERAL_CHANNEL_ID,
            "General Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Kabali Assistant general notifications"
        }
        notificationManager.createNotificationChannel(channel)
    }
}