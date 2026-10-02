package com.aalavandaanus.kabaliassistant.data.birthdays

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {

    @Query("SELECT * FROM birthdays")
    fun getAllBirthdays(): Flow<List<BirthdayEntity>>

    @Query("SELECT * FROM birthdays WHERE id = :id")
    suspend fun getBirthdayById(id: Int): BirthdayEntity?

    @Insert
    suspend fun insertBirthday(
        birthday: BirthdayEntity
    )

    @Update
    suspend fun updateBirthday(
        birthday: BirthdayEntity
    )

    @Delete
    suspend fun deleteBirthday(
        birthday: BirthdayEntity
    )
}