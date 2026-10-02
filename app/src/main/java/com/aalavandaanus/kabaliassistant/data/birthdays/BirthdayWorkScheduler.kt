package com.aalavandaanus.kabaliassistant.data.birthdays

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object BirthdayWorkScheduler {

    private const val WORK_NAME =
        "birthday_daily_notification_check"

    fun schedule(
        context: Context
    ) {

        val now = LocalDateTime.now()

        var nextRun =
            LocalDateTime.of(
                now.toLocalDate(),
                LocalTime.of(9, 0)
            )

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }

        val initialDelay =
            Duration.between(
                now,
                nextRun
            ).toMillis()

        val workRequest =
            PeriodicWorkRequestBuilder<
                    BirthdayNotificationWorker
                    >(
                24,
                TimeUnit.HOURS
            )
                .setInitialDelay(
                    initialDelay,
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager
            .getInstance(context)
            .enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
    }
}