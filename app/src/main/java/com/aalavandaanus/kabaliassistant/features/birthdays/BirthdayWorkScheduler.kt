package com.aalavandaanus.kabaliassistant.features.birthdays

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object BirthdayWorkScheduler {

    private const val UNIQUE_WORK_NAME =
        "kabali_daily_birthday_check"

    /** Schedule once without resetting an existing daily job. */
    fun ensureScheduled(
        context: Context,
        hour: Int,
        minute: Int
    ) {
        enqueue(context, hour, minute, replace = false)
    }

    /** Use when the user changes the configured notification time. */
    fun reschedule(
        context: Context,
        hour: Int,
        minute: Int
    ) {
        enqueue(context, hour, minute, replace = true)
    }

    private fun enqueue(
        context: Context,
        hour: Int,
        minute: Int,
        replace: Boolean
    ) {
        val now = Calendar.getInstance()

        val nextRun = (now.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour.coerceIn(0, 23))
            set(Calendar.MINUTE, minute.coerceIn(0, 59))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (!after(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay =
            (nextRun.timeInMillis - now.timeInMillis)
                .coerceAtLeast(0L)

        val request =
            PeriodicWorkRequestBuilder<BirthdayCheckWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(
                    initialDelay,
                    TimeUnit.MILLISECONDS
                )
                .build()

        WorkManager.getInstance(context.applicationContext)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                if (replace) {
                    ExistingPeriodicWorkPolicy.REPLACE
                } else {
                    ExistingPeriodicWorkPolicy.KEEP
                },
                request
            )
    }
}