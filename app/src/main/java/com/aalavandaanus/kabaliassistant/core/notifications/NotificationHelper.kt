package com.aalavandaanus.kabaliassistant.core.notifications

import android.Manifest
import android.R
import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationHelper(private val context: Context) { init {
    NotificationChannel.createChannels(context)
}


    fun show(request: NotificationRequest) {
        if (!hasNotificationPermission()) {
            return
        }
        val notification =
            NotificationCompat.Builder(context, NotificationChannel.getChannelId(request.channel))
                .setSmallIcon(R.drawable.ic_lock_idle_alarm).setContentTitle(request.title)
                .setContentText(request.message).setPriority(request.priority.toAndroidPriority())
                .setCategory(request.category.toAndroidCategory())
                .setVisibility(request.lockScreenVisibility.toAndroidVisibility())
                .setAutoCancel(request.autoCancel).setOngoing(request.ongoing)
                .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            NotificationManagerCompat.from(context).notify(
                request.id.toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // Permission may have changed between the check and notify().
        }
    }

    fun cancel(notificationId: Long) {
        NotificationManagerCompat.from(context).cancel(notificationId.toInt())
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun NotificationPriority.toAndroidPriority(): Int {
        return when (this) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.DEFAULT
                -> NotificationCompat.PRIORITY_DEFAULT

            NotificationPriority.HIGH
                -> NotificationCompat.PRIORITY_HIGH

            NotificationPriority.MAX
                -> NotificationCompat.PRIORITY_MAX
        }
    }

    private fun NotificationCategory.toAndroidCategory(): String {
        return when (this) {
            NotificationCategory.DEFAULT -> NotificationCompat.CATEGORY_STATUS
            NotificationCategory.ALARM
                -> NotificationCompat.CATEGORY_ALARM

            NotificationCategory.REMINDER
                -> NotificationCompat.CATEGORY_REMINDER

            NotificationCategory.EVENT
                -> NotificationCompat.CATEGORY_EVENT
        }
    }

    private fun LockScreenVisibility.toAndroidVisibility(): Int {
        return when (this) {
            LockScreenVisibility.SECRET -> NotificationCompat.VISIBILITY_SECRET
            LockScreenVisibility.PRIVATE
                -> NotificationCompat.VISIBILITY_PRIVATE

            LockScreenVisibility.PUBLIC
                -> NotificationCompat.VISIBILITY_PUBLIC
        }
    }
}