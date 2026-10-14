
package com.aalavandaanus.kabaliassistant.features.birthdays

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aalavandaanus.kabaliassistant.KabaliApplication
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationCategory
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationChannelType
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationHelper
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationPriority
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationRequest
import com.aalavandaanus.kabaliassistant.features.familytree.FamilyTreeData
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.first

class BirthdayCheckWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as KabaliApplication

            val birthdays = app.birthdayRepository
                .getAllBirthdays()
                .first()

            val today = LocalDate.now()
            val notifier = NotificationHelper(applicationContext)

            // Existing Room birthdays: preserve their reminder setting and ID.
            birthdays
                .asSequence()
                .filter { it.reminderEnabled }
                .forEach { birthday ->
                    notifyIfUpcoming(
                        name = birthday.name,
                        month = birthday.month,
                        day = birthday.day,
                        notificationId = birthday.id,
                        today = today,
                        notifier = notifier
                    )
                }

            // Family Tree birthdays: use the existing family data source.
            // Negative IDs keep these notifications separate from Room IDs.
            FamilyTreeData.people.forEach { person ->
                val birthDate = person.birthDate ?: return@forEach

                val familyNotificationId =
                    -(person.id.hashCode().toLong().absoluteValue + 1L)

                notifyIfUpcoming(
                    name = person.preferredName
                        ?.takeIf { it.isNotBlank() }
                        ?: person.fullName,
                    month = birthDate.monthValue,
                    day = birthDate.dayOfMonth,
                    notificationId = familyNotificationId,
                    today = today,
                    notifier = notifier
                )
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun notifyIfUpcoming(
        name: String,
        month: Int,
        day: Int,
        notificationId: Long,
        today: LocalDate,
        notifier: NotificationHelper
    ) {
        var birthdayDate = LocalDate.of(
            today.year,
            month,
            1
        ).plusDays((day - 1).toLong())

        if (birthdayDate.isBefore(today)) {
            birthdayDate = LocalDate.of(
                today.year + 1,
                month,
                1
            ).plusDays((day - 1).toLong())
        }

        val daysUntil = ChronoUnit.DAYS.between(
            today,
            birthdayDate
        ).toInt()

        if (daysUntil !in 0..5) return

        val message = when (daysUntil) {
            0 -> "Today is $name's birthday! 🎂"
            1 -> "$name's birthday is tomorrow."
            else -> "$name's birthday is in $daysUntil days."
        }

        notifier.show(
            NotificationRequest(
                id = notificationId,
                title = "Birthday Reminder",
                message = message,
                channel = NotificationChannelType.GENERAL,
                priority = NotificationPriority.HIGH,
                category = NotificationCategory.EVENT
            )
        )
    }
}
