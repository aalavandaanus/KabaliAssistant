package com.aalavandaanus.kabaliassistant.core.scheduling

import com.aalavandaanus.kabaliassistant.core.notifications.NotificationRequest

data class ScheduledEventResult(
    val notification: NotificationRequest?,
    val nextEvent: ScheduledEvent? = null
)