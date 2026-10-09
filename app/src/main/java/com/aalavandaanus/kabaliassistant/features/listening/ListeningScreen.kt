package com.aalavandaanus.kabaliassistant.features.listening

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.aalavandaanus.kabaliassistant.core.commands.CommandRouteResult
import com.aalavandaanus.kabaliassistant.core.commands.CommandRouter
import com.aalavandaanus.kabaliassistant.features.calling.AndroidCallService
import com.aalavandaanus.kabaliassistant.features.calling.AndroidContactRepository
import com.aalavandaanus.kabaliassistant.features.calling.CallCommandHandler
import com.aalavandaanus.kabaliassistant.features.calling.CallCommandParser
import com.aalavandaanus.kabaliassistant.features.calling.CallContactCommand
import com.aalavandaanus.kabaliassistant.features.calling.DefaultContactResolver

@Composable
fun ListeningScreen() {
    val context = LocalContext.current

    var status by remember { mutableStateOf("Ready to listen") }
    var recognizedText by remember { mutableStateOf("") }
    var commandResult by remember { mutableStateOf<CommandRouteResult?>(null) }

    // Increment this whenever we need to listen for a contact number.
    // The speech listener never calls the speech service directly.
    var selectionListenRequest by remember { mutableIntStateOf(0) }

    val commandRouter = remember {
        val contactRepository = AndroidContactRepository(context)
        val contactResolver = DefaultContactResolver()
        val callService = AndroidCallService(context)

        val callContactCommand = CallContactCommand(
            contactRepository = contactRepository,
            contactResolver = contactResolver,
            callService = callService
        )

        val callCommandHandler = CallCommandHandler(
            parser = CallCommandParser(),
            callContactCommand = callContactCommand
        )

        CommandRouter(callCommandHandler)
    }

    val speechService = remember {
        SpeechRecognitionService(
            context = context,
            listener = object : SpeechRecognitionListener {
                override fun onReady() {
                    status = "Listening…"
                }

                override fun onResult(text: String) {
                    recognizedText = text

                    val result = commandRouter.route(text)
                    commandResult = result

                    when (result) {
                        is CommandRouteResult.Ambiguous -> {
                            status = "Several contacts matched. Say a number."
                            selectionListenRequest++
                        }

                        is CommandRouteResult.SelectionInvalid -> {
                            status = "That number was not valid. Try again."
                            selectionListenRequest++
                        }

                        CommandRouteResult.SelectionCancelled -> {
                            status = "Contact selection cancelled."
                        }

                        is CommandRouteResult.Called -> {
                            status = "Opening dialer for ${result.contactName}"
                        }

                        CommandRouteResult.NotFound -> {
                            status = "Contact not found."
                        }

                        CommandRouteResult.NoPhoneNumber -> {
                            status = "The selected contact has no phone number."
                        }

                        CommandRouteResult.CallFailed -> {
                            status = "Could not open the dialer."
                        }

                        CommandRouteResult.NotHandled -> {
                            status = "Command not recognized."
                        }
                    }
                }


                override fun onError(errorCode: Int) {
                    status = "Speech recognition error code: $errorCode"
                    android.util.Log.e(
                        "KabaliSpeech",
                        "Speech recognition failed. Error code: $errorCode"
                    )
                }
            }
        )
    }

    // Start a new listening session after the contact list appears or an
    // invalid selection is received. No self-reference to speechService
    // is needed inside its own initialization.
    LaunchedEffect(selectionListenRequest) {
        val result = commandResult

        if (
            selectionListenRequest > 0 &&
            (result is CommandRouteResult.Ambiguous ||
                    result is CommandRouteResult.SelectionInvalid)
        ) {
            kotlinx.coroutines.delay(1000)
            speechService.startListening()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            speechService.startListening()
        } else {
            status = "Microphone permission is required."
        }
    }

    DisposableEffect(speechService) {
        onDispose {
            speechService.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Kabali Assistant",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(text = status)

        if (recognizedText.isNotBlank()) {
            Text(text = "Heard: $recognizedText")
        }

        Button(
            onClick = {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (hasPermission) {
                    speechService.startListening()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Listen")
        }

        if (
            commandResult is CommandRouteResult.Ambiguous ||
            commandResult is CommandRouteResult.SelectionInvalid
        ) {
            Button(
                onClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (hasPermission) {
                        speechService.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Listen for number")
            }
        }
    }

    val contactsToShow = when (val result = commandResult) {
        is CommandRouteResult.Ambiguous -> result.contactNames
        is CommandRouteResult.SelectionInvalid -> result.contactNames
        else -> null
    }

    if (contactsToShow != null) {
        AlertDialog(
            onDismissRequest = {
                commandRouter.route("cancel")
                commandResult = CommandRouteResult.SelectionCancelled
                status = "Contact selection cancelled."
            },
            title = {
                Text("Choose a contact")
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Say the number, such as “One,” or tap Listen for number.")

                    contactsToShow.forEachIndexed { index, name ->
                        Text("${index + 1}. $name")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            speechService.startListening()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                ) {
                    Text("Listen for number")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        commandRouter.route("cancel")
                        commandResult = CommandRouteResult.SelectionCancelled
                        status = "Contact selection cancelled."
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
