package com.aalavandaanus.kabaliassistant.data.birthdays

class BirthdayRepository(
    private val birthdayDao: BirthdayDao
) {

    suspend fun insertBirthday(
        birthday: BirthdayEntity
    ) {
        birthdayDao.insertBirthday(birthday)
    }

    suspend fun updateBirthday(
        birthday: BirthdayEntity
    ) {
        birthdayDao.updateBirthday(birthday)
    }

    suspend fun getBirthdayById(
        id: Int
    ): BirthdayEntity? {
        return birthdayDao.getBirthdayById(id)
    }

    suspend fun deleteBirthday(
        birthday: BirthdayEntity
    ) {
        birthdayDao.deleteBirthday(birthday)
    }
}