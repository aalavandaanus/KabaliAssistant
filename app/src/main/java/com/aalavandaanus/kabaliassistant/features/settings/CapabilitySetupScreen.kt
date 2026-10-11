
package com.aalavandaanus.kabaliassistant.features.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.aalavandaanus.kabaliassistant.core.capabilities.Capability
import com.aalavandaanus.kabaliassistant.core.capabilities.CapabilityChecker
import com.aalavandaanus.kabaliassistant.core.capabilities.CapabilityResult
import com.aalavandaanus.kabaliassistant.core.capabilities.CapabilityStatus

@Composable
fun CapabilitySetupScreen(
    onBack: () -> Unit,
    isFirstLaunch: Boolean = false
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val checker = remember(context) {
        CapabilityChecker(context)
    }

    var results by remember {
        mutableStateOf(checker.checkAll())
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            results = checker.checkAll()
        }

    val settingsLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            results = checker.checkAll()
        }

    DisposableEffect(lifecycleOwner, checker) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                results = checker.checkAll()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun requestPermission(capability: Capability) {
        val permission = when (capability) {
            Capability.MICROPHONE ->
                Manifest.permission.RECORD_AUDIO

            Capability.CONTACTS ->
                Manifest.permission.READ_CONTACTS

            Capability.NOTIFICATIONS ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.POST_NOTIFICATIONS
                } else {
                    null
                }

            else -> null
        }

        if (permission != null) {
            permissionLauncher.launch(permission)
        }
    }

    fun openCapabilitySettings(capability: Capability) {
        val intent = when (capability) {
            Capability.EXACT_ALARMS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:${context.packageName}")
                    )
                } else {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                }
            }

            Capability.NOTIFICATIONS -> {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(
                        Settings.EXTRA_APP_PACKAGE,
                        context.packageName
                    )
                }
            }

            else -> {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
            }
        }

        settingsLauncher.launch(intent)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (isFirstLaunch) {
                "Set up Kabali"
            } else {
                "Kabali Setup"
            },
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Review the capabilities below. Allow access only " +
                    "to features you want to use. You can change permissions " +
                    "later in Android Settings."
        )

        results.forEach { result ->
            val actionLabel = when (result.status) {
                CapabilityStatus.READY -> null

                CapabilityStatus.NEEDS_PERMISSION ->
                    when (result.capability) {
                        Capability.MICROPHONE -> "Allow microphone"
                        Capability.CONTACTS -> "Allow contacts"
                        Capability.NOTIFICATIONS -> "Allow notifications"
                        else -> null
                    }

                CapabilityStatus.NEEDS_SETTINGS ->
                    "Open Settings"

                CapabilityStatus.UNAVAILABLE -> null
            }

            CapabilityCard(
                result = result,
                actionLabel = actionLabel,
                onAction = {
                    when (result.status) {
                        CapabilityStatus.NEEDS_PERMISSION ->
                            requestPermission(result.capability)

                        CapabilityStatus.NEEDS_SETTINGS ->
                            openCapabilitySettings(result.capability)

                        else -> Unit
                    }
                }
            )
        }

        Button(
            onClick = {
                results = checker.checkAll()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh status")
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (isFirstLaunch) {
                    "Continue to Kabali"
                } else {
                    "Done"
                }
            )
        }
    }
}

@Composable
private fun CapabilityCard(
    result: CapabilityResult,
    actionLabel: String?,
    onAction: () -> Unit
) {
    val title = when (result.capability) {
        Capability.MICROPHONE -> "Microphone"
        Capability.CONTACTS -> "Contacts"
        Capability.NOTIFICATIONS -> "Notifications"
        Capability.EXACT_ALARMS -> "Exact alarms"
        Capability.SPEECH_RECOGNITION -> "Speech recognition"
    }

    val statusText = when (result.status) {
        CapabilityStatus.READY -> "Ready"
        CapabilityStatus.NEEDS_PERMISSION -> "Permission needed"
        CapabilityStatus.NEEDS_SETTINGS -> "Action needed in Settings"
        CapabilityStatus.UNAVAILABLE -> "Unavailable"
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = statusText,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = result.reason,
                style = MaterialTheme.typography.bodyMedium
            )

            if (actionLabel != null) {
                Button(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}