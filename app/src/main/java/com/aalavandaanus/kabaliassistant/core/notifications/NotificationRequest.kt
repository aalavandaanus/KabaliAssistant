package com.aalavandaanus.kabaliassistant.core.notifications

data class NotificationRequest(
    val id: Long,
    val title: String,
    val message: String,
    val channel: NotificationChannelType,
    val priority: NotificationPriority = NotificationPriority.DEFAULT,
    val category: NotificationCategory = NotificationCategory.DEFAULT,
    val autoCancel: Boolean = true,
    val ongoing: Boolean = false,
    val lockScreenVisibility: LockScreenVisibility =
        LockScreenVisibility.PUBLIC
)

enum class NotificationChannelType {
    ALARM,
    GENERAL
}

enum class NotificationPriority {
    LOW,
    DEFAULT,
    HIGH,
    MAX
}

enum class NotificationCategory {
    DEFAULT,
    ALARM,
    REMINDER,
    EVENT,
    SCHEDULE
}

enum class LockScreenVisibility {
    SECRET,
    PRIVATE,
    PUBLIC
}