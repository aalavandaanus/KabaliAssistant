package com.aalavandaanus.kabaliassistant.features.birthdays

import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduleRequest
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEvent
import com.aalavandaanus.kabaliassistant.core.scheduling.ScheduledEventType
import com.aalavandaanus.kabaliassistant.core.scheduling.Scheduler
import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository
import com.aalavandaanus.kabaliassistant.data.settings.AppSettingsRepository
import java.util.Calendar
import kotlinx.coroutines.flow.first

class BirthdayService(
    private val birthdayRepository: BirthdayRepository,
    private val scheduler: Scheduler,
    private val settingsRepository: AppSettingsRepository
) {

    suspend fun create(birthday: Birthday): Boolean {
        if (!isValidDate(birthday.month, birthday.day)) {
            return false
        }

        val id = birthdayRepository.insertBirthday(birthday)

        val savedBirthday = birthday.copy(id = id)

        if (!savedBirthday.reminderEnabled) {
            return true
        }

        val scheduled = scheduleBirthday(savedBirthday)

        if (!scheduled) {
            birthdayRepository.deleteBirthday(savedBirthday)
        }

        return scheduled
    }

    suspend fun update(birthday: Birthday): Boolean {
        scheduler.cancel(birthday.id)

        if (!isValidDate(birthday.month, birthday.day)) {
            return false
        }

        birthdayRepository.updateBirthday(birthday)

        if (!birthday.reminderEnabled) {
            return true
        }

        return scheduleBirthday(birthday)
    }

    suspend fun setEnabled(
        birthday: Birthday,
        enabled: Boolean
    ): Boolean {
        val updatedBirthday = birthday.copy(
            reminderEnabled = enabled
        )

        birthdayRepository.updateBirthday(updatedBirthday)

        if (!enabled) {
            scheduler.cancel(birthday.id)
            return true
        }

        return scheduleBirthday(updatedBirthday)
    }

    suspend fun delete(birthday: Birthday) {
        scheduler.cancel(birthday.id)
        birthdayRepository.deleteBirthday(birthday)
    }

    suspend fun rescheduleAll() {
        val birthdays =
            birthdayRepository
                .getAllBirthdays()
                .first()

        birthdays.forEach { birthday ->

            scheduler.cancel(birthday.id)

            if (birthday.reminderEnabled) {
                scheduleBirthday(birthday)
            }
        }
    }

    private fun scheduleBirthday(
        birthday: Birthday
    ): Boolean {

        val triggerAtMillis =
            calculateFirstReminderTime(birthday)

        return scheduler.schedule(
            ScheduleRequest(
                event = ScheduledEvent(
                    id = birthday.id,
                    type = ScheduledEventType.BIRTHDAY,
                    triggerAtMillis = triggerAtMillis,
                    enabled = birthday.reminderEnabled,
                    recurrence = Recurrence.None
                )
            )
        )
    }

    private fun calculateFirstReminderTime(
        birthday: Birthday
    ): Long {

        val now = Calendar.getInstance()

        val birthdayDate =
            createBirthdayCalendar(
                birthday = birthday,
                year = now.get(Calendar.YEAR)
            )

        val birthdayStart =
            birthdayDate.clone() as Calendar

        birthdayStart.set(
            Calendar.HOUR_OF_DAY,
            0
        )
        birthdayStart.set(
            Calendar.MINUTE,
            0
        )
        birthdayStart.set(
            Calendar.SECOND,
            0
        )
        birthdayStart.set(
            Calendar.MILLISECOND,
            0
        )

        val firstReminder =
            birthdayStart.clone() as Calendar

        firstReminder.add(
            Calendar.DAY_OF_YEAR,
            -5
        )

        setNotificationTime(firstReminder)

        /*
         * Current year's birthday window:
         *
         * birthday - 5 days
         * birthday - 4 days
         * birthday - 3 days
         * birthday - 2 days
         * birthday - 1 day
         * birthday
         */

        val windowStart =
            firstReminder.clone() as Calendar

        /*
         * If the birthday window has not started yet,
         * schedule the first reminder.
         */
        if (now.before(windowStart)) {
            return windowStart.timeInMillis
        }

        /*
         * If today is the birthday or one of the five
         * reminder days, schedule today's notification
         * if its configured time has not passed.
         */
        if (!now.after(birthdayStart)) {

            val todayNotification =
                Calendar.getInstance()

            setNotificationTime(todayNotification)

            if (todayNotification.timeInMillis > now.timeInMillis) {
                return todayNotification.timeInMillis
            }

            /*
             * Today's notification time has already passed.
             * Schedule tomorrow.
             */
            todayNotification.add(
                Calendar.DAY_OF_YEAR,
                1
            )

            setNotificationTime(todayNotification)

            /*
             * Tomorrow should still be inside the current
             * birthday window because we are currently
             * on or before the birthday.
             */
            return todayNotification.timeInMillis
        }

        /*
         * Current year's birthday window has completely
         * passed. Schedule next year's first reminder.
         */
        val nextBirthday =
            createBirthdayCalendar(
                birthday = birthday,
                year = now.get(Calendar.YEAR) + 1
            )

        val nextYearFirstReminder =
            nextBirthday.clone() as Calendar

        nextYearFirstReminder.add(
            Calendar.DAY_OF_YEAR,
            -5
        )

        setNotificationTime(nextYearFirstReminder)

        return nextYearFirstReminder.timeInMillis
    }

    private fun createBirthdayCalendar(
        birthday: Birthday,
        year: Int
    ): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, birthday.month - 1)
            set(Calendar.DAY_OF_MONTH, birthday.day)
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

    private fun isValidDate(
        month: Int,
        day: Int
    ): Boolean {

        if (month !in 1..12 || day !in 1..31) {
            return false
        }

        return try {

            Calendar.getInstance().apply {
                setLenient(false)
                set(Calendar.YEAR, 2000)
                set(Calendar.MONTH, month - 1)
                set(Calendar.DAY_OF_MONTH, day)
            }.time

            true

        } catch (_: Exception) {
            false
        }
    }
}
