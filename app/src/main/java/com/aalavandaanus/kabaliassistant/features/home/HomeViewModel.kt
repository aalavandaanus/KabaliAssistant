
package com.aalavandaanus.kabaliassistant.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository
import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository
import com.aalavandaanus.kabaliassistant.data.reminders.Reminder
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    alarmRepository: AlarmRepository,
    reminderRepository: ReminderRepository,
    birthdayRepository: BirthdayRepository
) : ViewModel() {

    private val refreshTrigger =
        MutableStateFlow(System.currentTimeMillis())

    val dashboardState = combine(
        alarmRepository.getAllAlarms(),
        reminderRepository.getAllReminders(),
        birthdayRepository.getAllBirthdays(),
        refreshTrigger
    ) { alarms, reminders, birthdays, _ ->
        calculateDashboard(
            alarms = alarms,
            reminders = reminders,
            birthdays = birthdays,
            nowMillis = System.currentTimeMillis()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeDashboardState()
    )

    /**
     * Recalculates time-sensitive dashboard information.
     * The screen will call this when the app returns to the foreground.
     */
    fun refresh() {
        refreshTrigger.value = System.currentTimeMillis()
    }
}

internal fun calculateDashboard(
    alarms: List<Alarm>,
    reminders: List<Reminder>,
    birthdays: List<Birthday>,
    nowMillis: Long
): HomeDashboardState {

    val nextAlarm = alarms
        .asSequence()
        .filter { it.enabled }
        .mapNotNull { alarm ->
            calculateNextAlarmTime(alarm, nowMillis)?.let { trigger ->
                NextAlarmInfo(
                    label = alarm.label.ifBlank { "Alarm" },
                    triggerAtMillis = trigger,
                    repeatDays = alarm.repeatDays
                )
            }
        }
        .minByOrNull { it.triggerAtMillis }


    val enabledReminders = reminders.filter {
        it.enabled && it.completedAtMillis == null
    }

    val upcomingReminder = enabledReminders
        .filter { it.triggerAtMillis > nowMillis }
        .minByOrNull { it.triggerAtMillis }
        ?.let { reminder ->
            ReminderInfo(
                title = reminder.title,
                message = reminder.message,
                triggerAtMillis = reminder.triggerAtMillis
            )
        }

    val overdueReminderCount = enabledReminders.count {
        it.triggerAtMillis <= nowMillis
    }

    val nearestBirthday = birthdays
        .mapNotNull { birthday ->
            calculateNextBirthday(birthday, nowMillis)
        }
        .minByOrNull { it.nextBirthdayAtMillis }

    return HomeDashboardState(
        nextAlarm = nextAlarm,
        upcomingReminder = upcomingReminder,
        overdueReminderCount = overdueReminderCount,
        nearestBirthday = nearestBirthday
    )
}

private fun calculateNextAlarmTime(
    alarm: Alarm,
    nowMillis: Long
): Long? {

    val now = Calendar.getInstance().apply {
        timeInMillis = nowMillis
    }

    val candidate = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, alarm.hour)
        set(Calendar.MINUTE, alarm.minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Preserve the existing one-time alarm display behavior:
    // if today's time has passed, show tomorrow's occurrence.
    if (alarm.repeatDays.isEmpty()) {
        if (candidate.timeInMillis <= nowMillis) {
            candidate.add(Calendar.DAY_OF_YEAR, 1)
        }
        return candidate.timeInMillis
    }

    // Repeat days use Calendar constants:
    // Sunday = 1 through Saturday = 7.
    val validRepeatDays = alarm.repeatDays.filter {
        it in Calendar.SUNDAY..Calendar.SATURDAY
    }.toSet()

    if (validRepeatDays.isEmpty()) {
        return null
    }

    for (daysAhead in 0..7) {
        val occurrence = candidate.clone() as Calendar
        occurrence.add(Calendar.DAY_OF_YEAR, daysAhead)

        if (
            occurrence.get(Calendar.DAY_OF_WEEK) in validRepeatDays &&
            occurrence.timeInMillis > nowMillis
        ) {
            return occurrence.timeInMillis
        }
    }

    return null
}

private fun calculateNextBirthday(
    birthday: Birthday,
    nowMillis: Long
): BirthdayInfo? {

    val zone = ZoneId.systemDefault()
    val now = java.time.Instant.ofEpochMilli(nowMillis)
        .atZone(zone)
        .toLocalDate()

    // Use the supplied clock for calculations, including tests.
    val currentDate = now

    fun birthdayDate(year: Int): LocalDate? {
        val day = if (
            birthday.month == 2 &&
            birthday.day == 29 &&
            !java.time.Year.isLeap(year.toLong())
        ) {
            28
        } else {
            birthday.day
        }

        return runCatching {
            LocalDate.of(year, birthday.month, day)
        }.getOrNull()
    }

    val thisYear = birthdayDate(currentDate.year) ?: return null
    val nextDate = if (thisYear.isBefore(currentDate)) {
        birthdayDate(currentDate.year + 1) ?: return null
    } else {
        thisYear
    }

    val daysUntil = ChronoUnit.DAYS.between(
        currentDate,
        nextDate
    ).toInt()

    val birthdayAtMillis = nextDate
        .atStartOfDay(zone)
        .toInstant()
        .toEpochMilli()

    return BirthdayInfo(
        name = birthday.name,
        relation = birthday.relation,
        nextBirthdayAtMillis = birthdayAtMillis,
        daysUntilBirthday = daysUntil
    )
}