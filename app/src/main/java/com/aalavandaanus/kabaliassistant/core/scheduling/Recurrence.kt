package com.aalavandaanus.kabaliassistant.core.scheduling

sealed interface Recurrence {

    data object None : Recurrence

    data object Daily : Recurrence

    data class Weekly(
        val daysOfWeek: Set<Int>
    ) : Recurrence

    data object Monthly : Recurrence

    data object Yearly : Recurrence
}