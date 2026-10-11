package com.aalavandaanus.kabaliassistant.features.birthdays

import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aalavandaanus.kabaliassistant.KabaliApplication
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationCategory
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationChannelType
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationHelper
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationPriority
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationRequest
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.flow.first


class BirthdayCheckWorker(
    appContext: android.content.Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val app =
                applicationContext as KabaliApplication

            val birthdays =
                app.birthdayRepository
                    .getAllBirthdays()
                    .first()

            val today = LocalDate.now()
            val notifier =
                NotificationHelper(applicationContext)

            birthdays
                .asSequence()
                .filter { it.reminderEnabled }
                .forEach { birthday ->

                    var birthdayDate =
                        LocalDate.of(
                            today.year,
                            birthday.month,
                            1
                        ).plusDays(
                            (birthday.day - 1).toLong()
                        )

                    // If this year's birthday has passed, use next year's date.
                    // This also handles birthdays around December and January.
                    if (birthdayDate.isBefore(today)) {
                        birthdayDate =
                            LocalDate.of(
                                today.year + 1,
                                birthday.month,
                                1
                            ).plusDays(
                                (birthday.day - 1).toLong()
                            )
                    }

                    val daysUntil =
                        ChronoUnit.DAYS.between(
                            today,
                            birthdayDate
                        ).toInt()

                    if (daysUntil in 0..5) {
                        val message = when (daysUntil) {
                            0 ->
                                "Today is ${birthday.name}'s birthday! 🎂"

                            1 ->
                                "${birthday.name}'s birthday is tomorrow."

                            else ->
                                "${birthday.name}'s birthday is in $daysUntil days."
                        }

                        notifier.show(
                            NotificationRequest(
                                id = birthday.id,
                                title = "Birthday Reminder",
                                message = message,
                                channel =
                                    NotificationChannelType.GENERAL,
                                priority =
                                    NotificationPriority.HIGH,
                                category =
                                    NotificationCategory.EVENT
                            )
                        )
                    }
                }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}