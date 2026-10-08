package com.aalavandaanus.kabaliassistant.data.birthdays

import kotlinx.coroutines.flow.Flow

interface BirthdayRepository {

    fun getAllBirthdays(): Flow<List<Birthday>>

    suspend fun getBirthdayById(id: Long): Birthday?

    suspend fun insertBirthday(birthday: Birthday): Long

    suspend fun updateBirthday(birthday: Birthday)

    suspend fun deleteBirthday(birthday: Birthday)
}