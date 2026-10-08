package com.aalavandaanus.kabaliassistant.features.birthdays

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aalavandaanus.kabaliassistant.data.birthdays.BirthdayRepository

class BirthdayViewModelFactory(
    private val birthdayRepository: BirthdayRepository,
    private val birthdayService: BirthdayService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(BirthdayViewModel::class.java)) {
            return BirthdayViewModel(
                birthdayRepository = birthdayRepository,
                birthdayService = birthdayService
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}