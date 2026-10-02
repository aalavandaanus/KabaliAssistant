package com.aalavandaanus.kabaliassistant.features.birthdays

import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayEntity
import java.time.LocalDate
import java.time.temporal.ChronoUnit

fun BirthdayEntity.getAge(): Int {
    val today = LocalDate.now()

    var age = today.year - year

    val birthdayThisYear = LocalDate.of(
        today.year,
        month,
        day
    )

    if (today.isBefore(birthdayThisYear)) {
        age--
    }

    return age
}

fun BirthdayEntity.getNextBirthday(): LocalDate {
    val today = LocalDate.now()

    var nextBirthday = LocalDate.of(
        today.year,
        month,
        day
    )

    if (nextBirthday.isBefore(today)) {
        nextBirthday = LocalDate.of(
            today.year + 1,
            month,
            day
        )
    }

    return nextBirthday
}

fun BirthdayEntity.getDaysUntilBirthday(): Long {
    val today = LocalDate.now()
    val nextBirthday = getNextBirthday()

    return ChronoUnit.DAYS.between(
        today,
        nextBirthday
    )
}