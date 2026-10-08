package com.aalavandaanus.kabaliassistant.data.birthdays

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomBirthdayRepository(
    private val birthdayDao: BirthdayDao
) : BirthdayRepository {

    override fun getAllBirthdays(): Flow<List<Birthday>> {
        return birthdayDao.getAllBirthdays().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBirthdayById(id: Long): Birthday? {
        return birthdayDao.getBirthdayById(id)?.toDomain()
    }

    override suspend fun insertBirthday(birthday: Birthday): Long {
        return birthdayDao.insertBirthday(birthday.toEntity())
    }

    override suspend fun updateBirthday(birthday: Birthday) {
        birthdayDao.updateBirthday(birthday.toEntity())
    }

    override suspend fun deleteBirthday(birthday: Birthday) {
        birthdayDao.deleteBirthday(birthday.toEntity())
    }

    private fun BirthdayEntity.toDomain(): Birthday {
        return Birthday(
            id = id,
            name = name,
            relation = relation,
            contactNumber = contactNumber,
            month = month,
            day = day,
            birthYear = birthYear,
            reminderEnabled = reminderEnabled
        )
    }

    private fun Birthday.toEntity(): BirthdayEntity {
        return BirthdayEntity(
            id = id,
            name = name,
            relation = relation,
            contactNumber = contactNumber,
            month = month,
            day = day,
            birthYear = birthYear,
            reminderEnabled = reminderEnabled
        )
    }
}