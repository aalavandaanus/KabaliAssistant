package com.aalavandaanus.kabaliassistant.data.birthdays

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class BirthdayNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(
    appContext,
    workerParams
) {

    override suspend fun doWork(): Result {

        return try {

            val database =
                BirthdayDatabase.getDatabase(applicationContext)

            val birthdayDao =
                database.birthdayDao()

            val birthdays =
                birthdayDao.getAllBirthdays().first()

            val today =
                LocalDate.now()

            for (birthday in birthdays) {

                if (!birthday.notificationEnabled) {
                    continue
                }

                val nextBirthday =
                    calculateNextBirthday(
                        today = today,
                        month = birthday.month,
                        day = birthday.day
                    )

                val daysUntilBirthday =
                    ChronoUnit.DAYS.between(
                        today,
                        nextBirthday
                    ).toInt()

                if (
                    daysUntilBirthday ==
                    birthday.notificationDaysBefore
                ) {

                    BirthdayNotificationHelper
                        .showBirthdayNotification(
                            context = applicationContext,
                            birthday = birthday
                        )
                }
            }

            Result.success()

        } catch (e: Exception) {

            e.printStackTrace()

            Result.retry()
        }
    }

    private fun calculateNextBirthday(
        today: LocalDate,
        month: Int,
        day: Int
    ): LocalDate {

        var birthday =
            LocalDate.of(
                today.year,
                month,
                day
            )

        if (birthday.isBefore(today)) {

            birthday =
                LocalDate.of(
                    today.year + 1,
                    month,
                    day
                )
        }

        return birthday
    }
}