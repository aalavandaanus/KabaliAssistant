package com.aalavandaanus.kabaliassistant.features.birthdays

import android.content.Context
import androidx.compose.foundation.style.apply
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationCategory
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationChannelType
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationPriority
import com.aalavandaanus.kabaliassistant.core.notifications.NotificationRequest
import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEvent
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventHandler
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventResult
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventType
import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository
import com.aalavandaanus.kabaliassistant.data.settings.AppSettingsRepository
import java.util.Calendar

class BirthdayScheduledEventHandler(
    private val birthdayRepository: BirthdayRepository,
    private val settingsRepository: AppSettingsRepository
) : ScheduledEventHandler {

    override val eventType =
        ScheduledEventType.BIRTHDAY

    override suspend fun handle(
        context: Context,
        eventId: Long
    ): ScheduledEventResult {

        val birthday =
            birthdayRepository.getBirthdayById(eventId)
                ?: return ScheduledEventResult(
                    notification = null
                )

        if (!birthday.reminderEnabled) {
            return ScheduledEventResult(
                notification = null
            )
        }

        val today =
            Calendar.getInstance()

        val birthdayDate =
            createBirthdayCalendar(
                birthday = birthday,
                year = today.get(Calendar.YEAR)
            )

        val todayStart =
            startOfDay(today)

        val daysUntilBirthday =
            daysBetween(
                from = todayStart,
                to = birthdayDate
            )

        /*
         * Notifications are valid only during:
         *
         * 5, 4, 3, 2, 1, 0 days before birthday.
         */
        val notification =
            if (daysUntilBirthday in 0..5) {

                createNotification(
                    birthday = birthday,
                    daysUntilBirthday = daysUntilBirthday
                )

            } else {
                null
            }

        return ScheduledEventResult(
            notification = notification,
            nextEvent = createNextEvent(
                birthday = birthday
            )
        )
    }

    private fun createNotification(
        birthday: Birthday,
        daysUntilBirthday: Int
    ): NotificationRequest {

        val message =
            when (daysUntilBirthday) {

                0 ->
                    "Today is ${birthday.name}'s birthday! 🎂"

                1 ->
                    "${birthday.name}'s birthday is tomorrow."

                else ->
                    "${birthday.name}'s birthday is in $daysUntilBirthday days."
            }

        return NotificationRequest(
            id = birthday.id,
            title = "Birthday Reminder",
            message = message,
            channel = NotificationChannelType.GENERAL,
            priority = NotificationPriority.HIGH,
            category = NotificationCategory.EVENT,
            autoCancel = true,
            ongoing = false
        )
    }

    private fun createNextEvent(
        birthday: Birthday
    ): ScheduledEvent {

        val now =
            Calendar.getInstance()

        val todayStart =
            startOfDay(now)

        val birthdayDate =
            createBirthdayCalendar(
                birthday = birthday,
                year = now.get(Calendar.YEAR)
            )

        val daysUntilBirthday =
            daysBetween(
                from = todayStart,
                to = birthdayDate
            )

        val nextCalendar =
            Calendar.getInstance()

        when {

            /*
             * We are still before the birthday.
             * Move to tomorrow.
             */
            daysUntilBirthday in 1..5 -> {

                nextCalendar.timeInMillis =
                    todayStart.timeInMillis

                nextCalendar.add(
                    Calendar.DAY_OF_YEAR,
                    1
                )
            }

            /*
             * Birthday notification has just fired.
             * The current year's window is complete.
             *
             * Schedule next year's first reminder:
             * birthday - 5 days.
             */
            daysUntilBirthday == 0 -> {

                val nextBirthday =
                    createBirthdayCalendar(
                        birthday = birthday,
                        year =
                            now.get(Calendar.YEAR) + 1
                    )

                nextCalendar.timeInMillis =
                    nextBirthday.timeInMillis

                nextCalendar.add(
                    Calendar.DAY_OF_YEAR,
                    -5
                )
            }

            /*
             * Current year's birthday has already passed.
             * Schedule next year's first reminder.
             */
            else -> {

                val nextBirthday =
                    createBirthdayCalendar(
                        birthday = birthday,
                        year =
                            now.get(Calendar.YEAR) + 1
                    )

                nextCalendar.timeInMillis =
                    nextBirthday.timeInMillis

                nextCalendar.add(
                    Calendar.DAY_OF_YEAR,
                    -5
                )
            }
        }

        setNotificationTime(nextCalendar)

        return ScheduledEvent(
            id = birthday.id,
            type = ScheduledEventType.BIRTHDAY,
            triggerAtMillis =
                nextCalendar.timeInMillis,
            enabled = birthday.reminderEnabled,
            recurrence = Recurrence.None
        )
    }

    private fun createBirthdayCalendar(
        birthday: Birthday,
        year: Int
    ): Calendar {

        return Calendar.getInstance().apply {

            set(Calendar.YEAR, year)

            set(
                Calendar.MONTH,
                birthday.month - 1
            )

            set(
                Calendar.DAY_OF_MONTH,
                birthday.day
            )

            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun setNotificationTime(
        calendar: Calendar
    ) {

        calendar.set(
            Calendar.HOUR_OF_DAY,
            settingsRepository.getBirthdayNotificationHour()
        )

        calendar.set(
            Calendar.MINUTE,
            settingsRepository.getBirthdayNotificationMinute()
        )

        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    private fun startOfDay(
        source: Calendar
    ): Calendar {
        val result = source.clone() as Calendar

        result.set(Calendar.HOUR_OF_DAY, 0)
        result.set(Calendar.MINUTE, 0)
        result.set(Calendar.SECOND, 0)
        result.set(Calendar.MILLISECOND, 0)

        return result
    }

    private fun daysBetween(
        from: Calendar,
        to: Calendar
    ): Int {

        val fromDay =
            startOfDay(from)

        val toDay =
            startOfDay(to)

        val difference =
            toDay.timeInMillis -
                    fromDay.timeInMillis

        return (
                difference / MILLIS_PER_DAY
                ).toInt()
    }

    companion object {

        private const val MILLIS_PER_DAY =
            24L * 60L * 60L * 1000L
    }
}
