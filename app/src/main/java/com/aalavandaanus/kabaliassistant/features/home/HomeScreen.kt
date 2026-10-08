package com.aalavandaanus.kabaliassistant.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    onOpenAlarms: () -> Unit = {},
    onOpenReminders: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kabali Assistant",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Your personal assistant",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}