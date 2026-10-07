package com.aalavandaanus.kabaliassistant.core.scheduling

class ScheduledEventHandlerRegistry(
    handlers: Set<ScheduledEventHandler>
) {

    private val handlersByType =
        handlers.associateBy {
            it.eventType
        }

    fun getHandler(
        eventType: ScheduledEventType
    ): ScheduledEventHandler? {
        return handlersByType[eventType]
    }
}