package com.aalavandaanus.kabaliassistant.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Person

@Composable
fun NavigationDrawer(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet {


        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Kabali Assistant",
                modifier = Modifier.padding(24.dp)
            )

            Divider()

            NavigationDrawerItem(
                label = {
                    Text("Home")
                },
                selected = currentRoute == Routes.HOME,
                onClick = {
                    onNavigate(Routes.HOME)
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home"
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )

            NavigationDrawerItem(
                label = {
                    Text("Alarms")
                },
                selected = currentRoute == Routes.ALARMS,
                onClick = {
                    onNavigate(Routes.ALARMS)
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Alarms"
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )

            NavigationDrawerItem(
                label = {
                    Text("Reminders")
                },
                selected = currentRoute == Routes.REMINDERS,
                onClick = {
                    onNavigate(Routes.REMINDERS)
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Reminders"
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )

            NavigationDrawerItem(
                label = {
                    Text("Birthdays")
                },
                selected = currentRoute == Routes.BIRTHDAYS,
                onClick = {
                    onNavigate(Routes.BIRTHDAYS)
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Cake,
                        contentDescription = "Birthdays"
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )

            NavigationDrawerItem(
                label = {
                    Text("Profile")
                },
                selected = currentRoute == Routes.PROFILE,
                onClick = {
                    onNavigate(Routes.PROFILE)
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile"
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )

            NavigationDrawerItem(
                label = {
                    Text("Listening")
                },
                selected = currentRoute == Routes.LISTENING,
                onClick = {
                    onNavigate(Routes.LISTENING)
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Listening"
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )

            Divider(
                modifier = Modifier.padding(
                    vertical = 8.dp
                )
            )

            NavigationDrawerItem(
                label = {
                    Text("Settings")
                },
                selected = currentRoute == Routes.SETTINGS,
                onClick = {
                    onNavigate(Routes.SETTINGS)
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                },
                modifier = Modifier.padding(
                    horizontal = 12.dp
                )
            )
        }
    }


}
