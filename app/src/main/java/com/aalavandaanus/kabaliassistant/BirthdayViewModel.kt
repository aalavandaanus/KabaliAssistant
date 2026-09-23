package com.aalavandaanus.kabaliassistant

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aalavandaanus.kabaliassistant.data.Birthday
import com.aalavandaanus.kabaliassistant.data.KabaliDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BirthdayViewModel(application: Application) :
    AndroidViewModel(application) {

    private val dao =
        KabaliDatabase
            .getDatabase(application)
            .birthdayDao()

    val birthdays: StateFlow<List<Birthday>> =
        dao.getAllBirthdays()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun addBirthday(
        name: String,
        date: String,
        relationship: String?
    ) {
        viewModelScope.launch {
            dao.insert(
                Birthday(
                    name = name.trim(),
                    date = date.trim(),
                    relationship = relationship
                        ?.trim()
                        ?.ifBlank { null }
                )
            )
        }
    }

    fun deleteBirthday(birthday: Birthday) {
        viewModelScope.launch {
            dao.delete(birthday)
        }
    }
}