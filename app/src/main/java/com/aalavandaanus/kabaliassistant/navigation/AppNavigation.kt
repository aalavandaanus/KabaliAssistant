package com.aalavandaanus.kabaliassistant.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.aalavandaanus.kabaliassistant.KabaliApplication
import com.aalavandaanus.kabaliassistant.features.alarms.AddAlarmScreen
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmListScreen
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmViewModel
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmViewModelFactory
import com.aalavandaanus.kabaliassistant.features.alarms.EditAlarmScreen
import com.aalavandaanus.kabaliassistant.features.home.HomeScreen

private object Routes {
    const val HOME = "home"
    const val ALARMS = "alarms"
    const val ADD_ALARM = "add_alarm"
    const val EDIT_ALARM = "edit_alarm/{alarmId}"

    fun editAlarm(alarmId: Long): String {
        return "edit_alarm/$alarmId"
    }
}

@Composable
fun AppNavigation(
    application: KabaliApplication
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {

        composable(Routes.HOME) {
            HomeScreen(
                onOpenAlarms = {
                    navController.navigate(Routes.ALARMS)
                }
            )
        }

        composable(Routes.ALARMS) {
            val viewModel: AlarmViewModel = viewModel(
                factory = AlarmViewModelFactory(
                    alarmRepository = application.alarmRepository,
                    alarmService = application.alarmService
                )
            )

            AlarmListScreen(
                viewModel = viewModel,
                onAddAlarm = {
                    navController.navigate(Routes.ADD_ALARM)
                },
                onEditAlarm = { alarmId ->
                    navController.navigate(
                        Routes.editAlarm(alarmId)
                    )
                }
            )
        }

        composable(Routes.ADD_ALARM) {
            val viewModel: AlarmViewModel = viewModel(
                factory = AlarmViewModelFactory(
                    alarmRepository = application.alarmRepository,
                    alarmService = application.alarmService
                )
            )

            AddAlarmScreen(
                viewModel = viewModel,
                onSaved = {
                    navController.popBackStack()
                },
                onCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.EDIT_ALARM,
            arguments = listOf(
                navArgument("alarmId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            val viewModel: AlarmViewModel = viewModel(
                factory = AlarmViewModelFactory(
                    alarmRepository = application.alarmRepository,
                    alarmService = application.alarmService
                )
            )

            val alarms by viewModel.alarms.collectAsStateWithLifecycle()

            val alarmId =
                backStackEntry.arguments?.getLong("alarmId")

            val alarm = alarms.firstOrNull {
                it.id == alarmId
            }

            if (alarm != null) {
                EditAlarmScreen(
                    alarm = alarm,
                    viewModel = viewModel,
                    onSaved = {
                        navController.popBackStack()
                    },
                    onDeleted = {
                        navController.popBackStack()
                    },
                    onCancel = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
