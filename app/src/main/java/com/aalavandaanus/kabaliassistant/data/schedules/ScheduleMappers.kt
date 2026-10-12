package com.aalavandaanus.kabaliassistant.data.schedules

import com.aalavandaanus.kabaliassistant.core.scheduling.Recurrence

fun ScheduleEntity.toDomain(): Schedule {
    val parsedRecurrence = when (recurrenceType.uppercase()) {
        "DAILY" -> Recurrence.Daily
        "WEEKLY" -> {
            val days = repeatDays
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toSet()

            Recurrence.Weekly(days)
        }
        "MONTHLY" -> Recurrence.Monthly
        "YEARLY" -> Recurrence.Yearly
        else -> Recurrence.None
    }

    return Schedule(
        id = id,
        title = title,
        message = message,
        categoryId = categoryId,
        triggerAtMillis = triggerAtMillis,
        recurrence = parsedRecurrence,
        enabled = enabled,
        completed = completed,
        lastCompletedAtMillis = lastCompletedAtMillis
    )
}

fun Schedule.toEntity(): ScheduleEntity {
    val type: String
    val days: String

    when (val value = recurrence) {
        Recurrence.None -> {
            type = "NONE"
            days = ""
        }
        Recurrence.Daily -> {
            type = "DAILY"
            days = ""
        }
        is Recurrence.Weekly -> {
            type = "WEEKLY"
            days = value.daysOfWeek.sorted().joinToString(",")
        }
        Recurrence.Monthly -> {
            type = "MONTHLY"
            days = ""
        }
        Recurrence.Yearly -> {
            type = "YEARLY"
            days = ""
        }
    }

    return ScheduleEntity(
        id = id,
        title = title,
        message = message,
        categoryId = categoryId,
        triggerAtMillis = triggerAtMillis,
        recurrenceType = type,
        repeatDays = days,
        enabled = enabled,
        completed = completed,
        lastCompletedAtMillis = lastCompletedAtMillis
    )
}

fun ScheduleCategoryEntity.toDomain() = ScheduleCategory(
    id = id,
    name = name,
    icon = icon,
    color = color,
    isBuiltIn = isBuiltIn,
    sortOrder = sortOrder
)

fun ScheduleCategory.toEntity() = ScheduleCategoryEntity(
    id = id,
    name = name,
    icon = icon,
    color = color,
    isBuiltIn = isBuiltIn,
    sortOrder = sortOrder
)