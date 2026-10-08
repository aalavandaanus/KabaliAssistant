package com.aalavandaanus.kabaliassistant.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.aalavandaanus.kabaliassistant.KabaliApplication
import com.aalavandaanus.kabaliassistant.features.alarms.AddAlarmScreen
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmListScreen
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmViewModel
import com.aalavandaanus.kabaliassistant.features.alarms.AlarmViewModelFactory
import com.aalavandaanus.kabaliassistant.features.alarms.EditAlarmScreen
import com.aalavandaanus.kabaliassistant.features.birthdays.AddBirthdayScreen
import com.aalavandaanus.kabaliassistant.features.birthdays.BirthdayListScreen
import com.aalavandaanus.kabaliassistant.features.birthdays.BirthdayViewModel
import com.aalavandaanus.kabaliassistant.features.birthdays.BirthdayViewModelFactory
import com.aalavandaanus.kabaliassistant.features.birthdays.EditBirthdayScreen
import com.aalavandaanus.kabaliassistant.features.home.HomeScreen
import com.aalavandaanus.kabaliassistant.features.reminders.AddReminderScreen
import com.aalavandaanus.kabaliassistant.features.reminders.EditReminderScreen
import com.aalavandaanus.kabaliassistant.features.reminders.ReminderListScreen
import com.aalavandaanus.kabaliassistant.features.reminders.ReminderViewModel
import com.aalavandaanus.kabaliassistant.features.reminders.ReminderViewModelFactory
import com.aalavandaanus.kabaliassistant.features.settings.SettingsScreen
import kotlinx.coroutines.launch

object Routes {

    const val HOME = "home"

    const val ALARMS = "alarms"
    const val ADD_ALARM = "add_alarm"
    const val EDIT_ALARM = "edit_alarm/{alarmId}"

    const val REMINDERS = "reminders"
    const val ADD_REMINDER = "add_reminder"
    const val EDIT_REMINDER = "edit_reminder/{reminderId}"

    const val BIRTHDAYS = "birthdays"
    const val ADD_BIRTHDAY = "add_birthday"
    const val EDIT_BIRTHDAY = "edit_birthday/{birthdayId}"

    const val SETTINGS = "settings"

    fun editAlarm(alarmId: Long) =
        "edit_alarm/$alarmId"

    fun editReminder(reminderId: Long) =
        "edit_reminder/$reminderId"

    fun editBirthday(birthdayId: Long) =
        "edit_birthday/$birthdayId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    application: KabaliApplication
) {
    val drawerState =
        rememberDrawerState(DrawerValue.Closed)

    val scope =
        rememberCoroutineScope()

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute =
        backStackEntry?.destination?.route

    val drawerRoute =
        when {
            currentRoute == Routes.HOME ->
                Routes.HOME

            currentRoute == Routes.ALARMS ||
                    currentRoute == Routes.ADD_ALARM ||
                    currentRoute == Routes.EDIT_ALARM ->
                Routes.ALARMS

            currentRoute == Routes.REMINDERS ||
                    currentRoute == Routes.ADD_REMINDER ||
                    currentRoute == Routes.EDIT_REMINDER ->
                Routes.REMINDERS

            currentRoute == Routes.BIRTHDAYS ||
                    currentRoute == Routes.ADD_BIRTHDAY ||
                    currentRoute == Routes.EDIT_BIRTHDAY ->
                Routes.BIRTHDAYS

            currentRoute == Routes.SETTINGS ->
                Routes.SETTINGS

            else ->
                null
        }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {

                NavigationDrawer(
                    currentRoute = drawerRoute,
                    onNavigate = { route ->

                        scope.launch {
                            drawerState.close()
                        }

                        navController.navigate(route) {

                            popUpTo(Routes.HOME) {
                                saveState = true
                            }

                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) {

        Scaffold(
            topBar = {

                TopAppBar(
                    title = {

                        Text(
                            when (drawerRoute) {

                                Routes.HOME ->
                                    "Home"

                                Routes.ALARMS ->
                                    "Alarms"

                                Routes.REMINDERS ->
                                    "Reminders"

                                Routes.BIRTHDAYS ->
                                    "Birthdays"

                                Routes.SETTINGS ->
                                    "Settings"

                                else ->
                                    "Kabali Assistant"
                            }
                        )
                    },
                    navigationIcon = {

                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {

                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->

            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.padding(paddingValues)
            ) {

                // ---------------------------------------------------------
                // HOME
                // ---------------------------------------------------------

                composable(Routes.HOME) {

                    HomeScreen()
                }

                // ---------------------------------------------------------
                // ALARMS
                // ---------------------------------------------------------

                composable(Routes.ALARMS) {

                    val viewModel: AlarmViewModel =
                        viewModel(
                            factory = AlarmViewModelFactory(
                                alarmRepository = application.alarmRepository,
                                alarmService = application.alarmService
                            )
                        )

                    AlarmListScreen(
                        viewModel = viewModel,
                        onAddAlarm = {
                            navController.navigate(
                                Routes.ADD_ALARM
                            )
                        },
                        onEditAlarm = { alarmId ->
                            navController.navigate(
                                Routes.editAlarm(alarmId)
                            )
                        }
                    )
                }

                composable(Routes.ADD_ALARM) {

                    val viewModel: AlarmViewModel =
                        viewModel(
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
                ) { entry ->

                    val alarmId =
                        entry.arguments
                            ?.getLong("alarmId")
                            ?: return@composable

                    val viewModel: AlarmViewModel =
                        viewModel(
                            factory = AlarmViewModelFactory(
                                alarmRepository = application.alarmRepository,
                                alarmService = application.alarmService
                            )
                        )

                    val alarms by
                    viewModel.alarms.collectAsState()

                    val alarm =
                        alarms.firstOrNull {
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

                // ---------------------------------------------------------
                // REMINDERS
                // ---------------------------------------------------------

                composable(Routes.REMINDERS) {

                    val viewModel: ReminderViewModel =
                        viewModel(
                            factory = ReminderViewModelFactory(
                                reminderRepository = application.reminderRepository,
                                reminderService = application.reminderService
                            )
                        )

                    ReminderListScreen(
                        viewModel = viewModel,
                        onAddReminder = {
                            navController.navigate(
                                Routes.ADD_REMINDER
                            )
                        },
                        onEditReminder = { reminderId ->
                            navController.navigate(
                                Routes.editReminder(reminderId)
                            )
                        }
                    )
                }

                composable(Routes.ADD_REMINDER) {

                    val viewModel: ReminderViewModel =
                        viewModel(
                            factory = ReminderViewModelFactory(
                                reminderRepository = application.reminderRepository,
                                reminderService = application.reminderService
                            )
                        )

                    AddReminderScreen(
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
                    route = Routes.EDIT_REMINDER,
                    arguments = listOf(
                        navArgument("reminderId") {
                            type = NavType.LongType
                        }
                    )
                ) { entry ->

                    val reminderId =
                        entry.arguments
                            ?.getLong("reminderId")
                            ?: return@composable

                    val viewModel: ReminderViewModel =
                        viewModel(
                            factory = ReminderViewModelFactory(
                                reminderRepository = application.reminderRepository,
                                reminderService = application.reminderService
                            )
                        )

                    val reminders by
                    viewModel.reminders.collectAsState()

                    val reminder =
                        reminders.firstOrNull {
                            it.id == reminderId
                        }

                    if (reminder != null) {

                        EditReminderScreen(
                            reminder = reminder,
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

                // ---------------------------------------------------------
                // BIRTHDAYS
                // ---------------------------------------------------------

                composable(Routes.BIRTHDAYS) {

                    val viewModel: BirthdayViewModel =
                        viewModel(
                            factory = BirthdayViewModelFactory(
                                birthdayRepository = application.birthdayRepository,
                                birthdayService = application.birthdayService
                            )
                        )

                    BirthdayListScreen(
                        viewModel = viewModel,
                        onAddBirthday = {
                            navController.navigate(
                                Routes.ADD_BIRTHDAY
                            )
                        },
                        onEditBirthday = { birthdayId ->
                            navController.navigate(
                                Routes.editBirthday(birthdayId)
                            )
                        }
                    )
                }

                composable(Routes.ADD_BIRTHDAY) {

                    val viewModel: BirthdayViewModel =
                        viewModel(
                            factory = BirthdayViewModelFactory(
                                birthdayRepository = application.birthdayRepository,
                                birthdayService = application.birthdayService
                            )
                        )

                    AddBirthdayScreen(
                        viewModel = viewModel,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = Routes.EDIT_BIRTHDAY,
                    arguments = listOf(
                        navArgument("birthdayId") {
                            type = NavType.LongType
                        }
                    )
                ) { entry ->

                    val birthdayId =
                        entry.arguments
                            ?.getLong("birthdayId")
                            ?: return@composable

                    val viewModel: BirthdayViewModel =
                        viewModel(
                            factory = BirthdayViewModelFactory(
                                birthdayRepository = application.birthdayRepository,
                                birthdayService = application.birthdayService
                            )
                        )

                    val birthdays by
                    viewModel.birthdays.collectAsState()

                    val birthday =
                        birthdays.firstOrNull {
                            it.id == birthdayId
                        }

                    if (birthday != null) {

                        EditBirthdayScreen(
                            birthday = birthday,
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

                // ---------------------------------------------------------
                // SETTINGS
                // ---------------------------------------------------------

                composable(Routes.SETTINGS) {

                    SettingsScreen(
                        settingsRepository = application.appSettingsRepository,
                        birthdayService = application.birthdayService,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
