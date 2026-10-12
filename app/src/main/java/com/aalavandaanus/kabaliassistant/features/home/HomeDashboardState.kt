
package com.aalavandaanus.kabaliassistant.features.home

data class HomeDashboardState(
    val nextAlarm: NextAlarmInfo? = null,
    val upcomingReminder: ReminderInfo? = null,
    val overdueReminderCount: Int = 0,
    val nearestBirthday: BirthdayInfo? = null,
    val upcomingSchedules: List<UpcomingScheduleInfo> = emptyList(),
    val overdueScheduleCount: Int = 0
)

data class NextAlarmInfo(
    val label: String,
    val triggerAtMillis: Long,
    val repeatDays: Set<Int>
)

data class ReminderInfo(
    val title: String,
    val message: String,
    val triggerAtMillis: Long
)

data class BirthdayInfo(
    val name: String,
    val relation: String,
    val nextBirthdayAtMillis: Long,
    val daysUntilBirthday: Int
)

data class UpcomingScheduleInfo(
    val id: Long,
    val title: String,
    val message: String,
    val categoryName: String,
    val categoryIcon: String,
    val triggerAtMillis: Long,
    val isOverdue: Boolean
)