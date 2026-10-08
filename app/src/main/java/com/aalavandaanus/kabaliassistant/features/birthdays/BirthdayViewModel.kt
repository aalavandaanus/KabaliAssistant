package com.aalavandaanus.kabaliassistant.features.birthdays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aalavandaanus.kabaliassistant.data.birthdays.Birthday
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BirthdayViewModel(
    private val birthdayRepository: BirthdayRepository,
    private val birthdayService: BirthdayService
) : ViewModel() {

    val birthdays: StateFlow<List<Birthday>> =
        birthdayRepository
            .getAllBirthdays()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = emptyList()
            )

    fun createBirthday(
        birthday: Birthday,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = birthdayService.create(birthday)
            onResult?.invoke(result)
        }
    }

    fun updateBirthday(
        birthday: Birthday,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = birthdayService.update(birthday)
            onResult?.invoke(result)
        }
    }

    fun setBirthdayEnabled(
        birthday: Birthday,
        enabled: Boolean,
        onResult: ((Boolean) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = birthdayService.setEnabled(
                birthday = birthday,
                enabled = enabled
            )

            onResult?.invoke(result)
        }
    }

    fun deleteBirthday(
        birthday: Birthday,
        onResult: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            birthdayService.delete(birthday)
            onResult?.invoke()
        }
    }
}