
package com.aalavandaanus.kabaliassistant.core.notifications

import android.Manifest
import android.R
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class NotificationHelper(private val context: Context) {

    init {
        NotificationChannel.createChannels(context)
    }

    fun show(request: NotificationRequest) {
        if (!hasNotificationPermission()) return

        val notification = NotificationCompat.Builder(
            context,
            NotificationChannel.getChannelId(request.channel)
        )
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle(request.title)
            .setContentText(request.message)
            .setPriority(request.priority.toAndroidPriority())
            .setCategory(request.category.toAndroidCategory())
            .setVisibility(request.lockScreenVisibility.toAndroidVisibility())
            .setAutoCancel(request.autoCancel)
            .setOngoing(request.ongoing)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                notificationId(request.id, request.category),
                notification
            )
        } catch (_: SecurityException) {
            // Notification permission may have changed between checks.
        }
    }

    /**
     * Cancel one feature's notification without affecting another
     * feature that happens to have the same database ID.
     */
    fun cancel(id: Long, category: NotificationCategory) {
        NotificationManagerCompat.from(context).cancel(
            notificationId(id, category)
        )
    }

    /**
     * Legacy cancellation API: cancel the matching ID in every
     * category, since the caller did not identify the feature.
     */
    fun cancel(notificationId: Long) {
        NotificationCategory.entries.forEach { category ->
            cancel(notificationId, category)
        }
    }

    private fun notificationId(
        id: Long,
        category: NotificationCategory
    ): Int {
        // Reserve the high 4 bits for the category namespace.
        // Database IDs are normally small auto-generated values.
        val categoryBits = category.ordinal shl 28
        val idBits = id.toInt() and 0x0FFFFFFF
        return categoryBits or idBits
    }

    private fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun NotificationPriority.toAndroidPriority(): Int =
        when (this) {
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
            NotificationPriority.DEFAULT -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.MAX -> NotificationCompat.PRIORITY_MAX
        }

    private fun NotificationCategory.toAndroidCategory(): String =
        when (this) {
            NotificationCategory.DEFAULT -> NotificationCompat.CATEGORY_STATUS
            NotificationCategory.ALARM -> NotificationCompat.CATEGORY_ALARM
            NotificationCategory.REMINDER,
            NotificationCategory.SCHEDULE -> NotificationCompat.CATEGORY_REMINDER
            NotificationCategory.EVENT -> NotificationCompat.CATEGORY_EVENT
        }

    private fun LockScreenVisibility.toAndroidVisibility(): Int =
        when (this) {
            LockScreenVisibility.SECRET -> NotificationCompat.VISIBILITY_SECRET
            LockScreenVisibility.PRIVATE -> NotificationCompat.VISIBILITY_PRIVATE
            LockScreenVisibility.PUBLIC -> NotificationCompat.VISIBILITY_PUBLIC
        }
}