
package com.aalavandaanus.kabaliassistant.features.birthdays

import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository
import java.util.Calendar

class BirthdayService(
    private val birthdayRepository: BirthdayRepository
) {

    suspend fun create(birthday: Birthday): Boolean {
        if (!isValidDate(birthday.month, birthday.day)) {
            return false
        }

        birthdayRepository.insertBirthday(birthday)
        return true
    }

    suspend fun update(birthday: Birthday): Boolean {
        if (!isValidDate(birthday.month, birthday.day)) {
            return false
        }

        birthdayRepository.updateBirthday(birthday)
        return true
    }

    suspend fun setEnabled(
        birthday: Birthday,
        enabled: Boolean
    ): Boolean {
        birthdayRepository.updateBirthday(
            birthday.copy(reminderEnabled = enabled)
        )
        return true
    }

    suspend fun delete(birthday: Birthday) {
        birthdayRepository.deleteBirthday(birthday)
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
                isLenient = false
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