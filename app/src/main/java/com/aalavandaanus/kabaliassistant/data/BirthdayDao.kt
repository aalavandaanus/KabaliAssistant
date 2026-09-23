package com.aalavandaanus.kabaliassistant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BirthdayDao {

    @Query("SELECT * FROM birthdays ORDER BY date")
    fun getAllBirthdays(): Flow<List<Birthday>>

    @Query("SELECT * FROM birthdays WHERE id = :id")
    suspend fun getBirthday(id: Long): Birthday?

    @Insert
    suspend fun insert(birthday: Birthday): Long

    @Update
    suspend fun update(birthday: Birthday)

    @Delete
    suspend fun delete(birthday: Birthday)
}