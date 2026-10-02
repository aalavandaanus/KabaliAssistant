package com.aalavandaanus.kabaliassistant.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NavigationDrawer(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    ModalDrawerSheet {

        Text(
            text = "KABALI",
            modifier = Modifier.padding(
                horizontal = 24.dp,
                vertical = 24.dp
            )
        )

        NavigationDrawerItem(
            label = { Text("Home") },
            selected = currentRoute == "home",
            onClick = {
                onNavigate("home")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            }
        )

        NavigationDrawerItem(
            label = { Text("Listening") },
            selected = currentRoute == "listening",
            onClick = {
                onNavigate("listening")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Listening"
                )
            }
        )

        NavigationDrawerItem(
            label = { Text("Birthdays") },
            selected = currentRoute == "birthdays",
            onClick = {
                onNavigate("birthdays")
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Cake,
                    contentDescription = "Birthdays"
                )
            }
        )
    }
}