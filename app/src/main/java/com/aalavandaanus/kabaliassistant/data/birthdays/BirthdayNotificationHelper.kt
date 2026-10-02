package com.aalavandaanus.kabaliassistant.data.birthdays

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aalavandaanus.kabaliassistant.MainActivity

object BirthdayNotificationHelper {

    private const val CHANNEL_ID = "birthday_reminders"

    private const val CHANNEL_NAME =
        "Birthday Reminders"

    private const val CHANNEL_DESCRIPTION =
        "Reminders for upcoming birthdays"

    fun createNotificationChannel(
        context: Context
    ) {

        val channel =
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {

                description =
                    CHANNEL_DESCRIPTION
            }

        val notificationManager =
            context.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        notificationManager.createNotificationChannel(
            channel
        )
    }

    fun showBirthdayNotification(
        context: Context,
        birthday: BirthdayEntity
    ) {

        createNotificationChannel(context)

        if (
            android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
        }

        val openAppIntent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {

                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                birthday.id,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val message =
            if (
                birthday.notificationDaysBefore == 0
            ) {

                "Today is ${birthday.name}'s birthday! 🎂"

            } else {

                "${birthday.name}'s birthday is " +
                        "in ${birthday.notificationDaysBefore} " +
                        "day" +
                        if (
                            birthday.notificationDaysBefore == 1
                        ) {
                            ""
                        } else {
                            "s"
                        } +
                        "! 🎂"
            }

        val notification =
            NotificationCompat.Builder(
                context,
                CHANNEL_ID
            )
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "Birthday Reminder 🎉"
                )
                .setContentText(
                    message
                )
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(message)
                )
                .setPriority(
                    NotificationCompat.PRIORITY_DEFAULT
                )
                .setAutoCancel(true)
                .setContentIntent(
                    pendingIntent
                )
                .build()

        NotificationManagerCompat
            .from(context)
            .notify(
                birthday.id,
                notification
            )
    }
}