package com.aalavandaanus.kabaliassistant.features.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aalavandaanus.kabaliassistant.data.settings.AppSettingsRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.aalavandaanus.kabaliassistant.features.birthdays.BirthdayWorkScheduler

@Composable
fun SettingsScreen(
    settingsRepository: AppSettingsRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current


    var notificationHour by remember {
        mutableStateOf(
            settingsRepository.getBirthdayNotificationHour()
        )
    }

    var notificationMinute by remember {
        mutableStateOf(
            settingsRepository.getBirthdayNotificationMinute()
        )
    }

    fun formattedTime(
        hour: Int,
        minute: Int
    ): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        return SimpleDateFormat(
            "hh:mm a",
            Locale.getDefault()
        ).format(calendar.time)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Settings"
        )

        Text(
            text = "Birthday Notifications"
        )

        OutlinedTextField(
            value = formattedTime(
                notificationHour,
                notificationMinute
            ),
            onValueChange = {},
            readOnly = true,
            label = {
                Text("Notification Time")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {

                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->

                        notificationHour = hourOfDay
                        notificationMinute = minute

                        settingsRepository.setBirthdayNotificationTime(
                            hourOfDay,
                            minute
                        )

                        BirthdayWorkScheduler.reschedule(
                            context = context,
                            hour = hourOfDay,
                            minute = minute
                        )
                    },
                    notificationHour,
                    notificationMinute,
                    false
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Change Notification Time")
        }

        Text(
            text = "Birthday reminders will be sent at this time during the birthday reminder window."
        )

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}