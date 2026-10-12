
package com.aalavandaanus.kabaliassistant.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalavandaanus.kabaliassistant.data.alarms.Alarm
import com.aalavandaanus.kabaliassistant.data.alarms.AlarmRepository
import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository
import com.aalavandaanus.kabaliassistant.data.reminders.Reminder
import com.aalavandaanus.kabaliassistant.data.reminders.ReminderRepository
import com.aalavandaanus.kabaliassistant.data.schedules.Schedule
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategory
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleCategoryRepository
import com.aalavandaanus.kabaliassistant.data.schedules.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.Year
import java.time.ZoneId
import java.util.Calendar


private data class HomeDashboardInputs(
    val alarms: List<Alarm>,
    val reminders: List<Reminder>,
    val birthdays: List<Birthday>,
    val schedules: List<Schedule>,
    val categories: List<ScheduleCategory>
)

class HomeViewModel(
    alarmRepository: AlarmRepository,
    reminderRepository: ReminderRepository,
    birthdayRepository: BirthdayRepository,
    scheduleRepository: ScheduleRepository,
    categoryRepository: ScheduleCategoryRepository
) : ViewModel() {

    private val refreshTrigger = MutableStateFlow(0)

    val dashboardState = combine(
        alarmRepository.getAllAlarms(),
        reminderRepository.getAllReminders(),
        birthdayRepository.getAllBirthdays(),
        scheduleRepository.getAllSchedules(),
        categoryRepository.getAllCategories()
    ) { alarms, reminders, birthdays, schedules, categories ->
        HomeDashboardInputs(
            alarms = alarms,
            reminders = reminders,
            birthdays = birthdays,
            schedules = schedules,
            categories = categories
        )
    }.combine(refreshTrigger) { input, _ ->
        calculateDashboard(
            alarms = input.alarms,
            reminders = input.reminders,
            birthdays = input.birthdays,
            schedules = input.schedules,
            categories = input.categories,
            nowMillis = System.currentTimeMillis()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = HomeDashboardState()
    )

    fun refresh() {
        refreshTrigger.value++
    }
}

internal fun calculateDashboard(
    alarms: List<Alarm>,
    reminders: List<Reminder>,
    birthdays: List<Birthday>,
    nowMillis: Long,
    schedules: List<Schedule> = emptyList(),
    categories: List<ScheduleCategory> = emptyList()
): HomeDashboardState {

    // Next enabled alarm
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

    // Legacy reminders: retain existing dashboard values for compatibility.
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

    // New unified schedules
    val categoryById = categories.associateBy { it.id }

    val activeSchedules = schedules.filter {
        it.enabled && !it.completed
    }

    val upcomingSchedules = activeSchedules
        .sortedBy { it.triggerAtMillis }
        .take(3)
        .map { schedule ->
            val category = categoryById[schedule.categoryId]

            UpcomingScheduleInfo(
                id = schedule.id,
                title = schedule.title,
                message = schedule.message,
                categoryName = category?.name ?: "General",
                categoryIcon = category?.icon ?: "📌",
                triggerAtMillis = schedule.triggerAtMillis,
                isOverdue = schedule.triggerAtMillis <= nowMillis
            )
        }

    val overdueScheduleCount = activeSchedules.count {
        it.triggerAtMillis <= nowMillis
    }

    // Nearest birthday
    val nearestBirthday = birthdays
        .mapNotNull { calculateNextBirthday(it, nowMillis) }
        .minByOrNull { it.nextBirthdayAtMillis }

    return HomeDashboardState(
        nextAlarm = nextAlarm,
        upcomingReminder = upcomingReminder,
        overdueReminderCount = overdueReminderCount,
        nearestBirthday = nearestBirthday,
        upcomingSchedules = upcomingSchedules,
        overdueScheduleCount = overdueScheduleCount
    )
}

internal fun calculateNextAlarmTime(
    alarm: Alarm,
    nowMillis: Long
): Long? {
    val zone = ZoneId.systemDefault()
    val now = java.time.Instant.ofEpochMilli(nowMillis)
        .atZone(zone)

    val todayAlarm = now.toLocalDate()
        .atTime(alarm.hour, alarm.minute)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()

    if (alarm.repeatDays.isEmpty()) {
        return if (todayAlarm > nowMillis) {
            todayAlarm
        } else {
            null
        }
    }

    for (daysAhead in 0..7) {
        val date = now.toLocalDate().plusDays(daysAhead.toLong())
        val dayOfWeek = date.dayOfWeek.value % 7 + 1

        if (dayOfWeek in alarm.repeatDays) {
            val candidate = date
                .atTime(alarm.hour, alarm.minute)
                .atZone(zone)
                .toInstant()
                .toEpochMilli()

            if (candidate > nowMillis) {
                return candidate
            }
        }
    }

    return null
}

internal fun calculateNextBirthday(
    birthday: Birthday,
    nowMillis: Long
): BirthdayInfo? {
    val today = java.time.Instant.ofEpochMilli(nowMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val birthdayDate = try {
        LocalDate.of(today.year, birthday.month, birthday.day)
    } catch (_: Exception) {
        return null
    }

    var nextBirthday = birthdayDate

    if (nextBirthday.isBefore(today)) {
        val nextYear = today.year + 1

        nextBirthday = if (
            birthday.month == 2 &&
            birthday.day == 29 &&
            !Year.isLeap(nextYear.toLong())
        ) {
            LocalDate.of(nextYear, 2, 28)
        } else {
            LocalDate.of(nextYear, birthday.month, birthday.day)
        }
    }

    val birthdayAtMillis = nextBirthday
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    return BirthdayInfo(
        name = birthday.name,
        relation = birthday.relation,
        nextBirthdayAtMillis = birthdayAtMillis,
        daysUntilBirthday = java.time.temporal.ChronoUnit.DAYS
            .between(today, nextBirthday)
            .toInt()
    )
}