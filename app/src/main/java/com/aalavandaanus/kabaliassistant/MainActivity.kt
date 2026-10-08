package com.aalavandaanus.kabaliassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.aalavandaanus.kabaliassistant.navigation.AppNavigation
import com.aalavandaanus.kabaliassistant.ui.theme.KabaliAssistantTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(
            savedInstanceState
        )

        val application =
            application as KabaliApplication

        setContent {

            val navController =
                rememberNavController()

            KabaliAssistantTheme {

                AppNavigation(
                    navController = navController,
                    application = application
                )
            }
        }
    }
}