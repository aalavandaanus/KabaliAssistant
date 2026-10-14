
package com.aalavandaanus.kabaliassistant.navigation

import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.aalavandaanus.kabaliassistant.KabaliApplication
import com.aalavandaanus.kabaliassistant.features.backup.BackupRestoreScreen
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
import com.aalavandaanus.kabaliassistant.features.home.HomeViewModel
import com.aalavandaanus.kabaliassistant.features.home.HomeViewModelFactory
import com.aalavandaanus.kabaliassistant.features.listening.ListeningScreen
import com.aalavandaanus.kabaliassistant.features.profile.ProfileScreen
import com.aalavandaanus.kabaliassistant.features.schedules.EditScheduleScreen
import com.aalavandaanus.kabaliassistant.features.schedules.ManageScheduleCategoriesScreen
import com.aalavandaanus.kabaliassistant.features.schedules.ScheduleListScreen
import com.aalavandaanus.kabaliassistant.features.schedules.ScheduleViewModel
import com.aalavandaanus.kabaliassistant.features.schedules.ScheduleViewModelFactory
import com.aalavandaanus.kabaliassistant.features.settings.CapabilitySetupScreen
import com.aalavandaanus.kabaliassistant.features.settings.SettingsScreen
import kotlinx.coroutines.launch
import com.aalavandaanus.kabaliassistant.features.familytree.FamilyTreeScreen

object Routes {
    const val HOME = "home"

    const val ALARMS = "alarms"
    const val ADD_ALARM = "add_alarm"
    const val EDIT_ALARM = "edit_alarm/{alarmId}"

    const val BIRTHDAYS = "birthdays"
    const val ADD_BIRTHDAY = "add_birthday"
    const val EDIT_BIRTHDAY = "edit_birthday/{birthdayId}"

    const val SETTINGS = "settings"
    const val CAPABILITY_SETUP = "capability_setup"

    // Existing route identifiers are retained.
    const val REMINDERS = "schedules"
    const val ADD_REMINDER = "add_schedule"
    const val EDIT_REMINDER = "edit_schedule/{scheduleId}"
    const val MANAGE_SCHEDULE_CATEGORIES =
        "manage_schedule_categories"
    const val FAMILY_TREE = "family_tree"
    const val LISTENING = "listening"
    const val PROFILE = "profile"
    const val BACKUP_RESTORE = "backup_restore"

    fun editReminder(scheduleId: Long) =
        "edit_schedule/$scheduleId"

    fun editAlarm(alarmId: Long) =
        "edit_alarm/$alarmId"

    fun editBirthday(birthdayId: Long) =
        "edit_birthday/$birthdayId"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    application: KabaliApplication
) {
    val context = LocalContext.current

    val assistantName by
    application.appSettingsRepository.assistantName.collectAsState()

    val onboardingPreferences = remember(context) {
        context.getSharedPreferences(
            "kabali_onboarding",
            Context.MODE_PRIVATE
        )
    }

    var onboardingComplete by remember {
        mutableStateOf(
            onboardingPreferences.getBoolean(
                "capability_setup_completed",
                false
            )
        )
    }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val backStackEntry by
    navController.currentBackStackEntryAsState()

    val currentRoute = backStackEntry?.destination?.route

    val isInitialSetup =
        currentRoute == Routes.CAPABILITY_SETUP &&
                !onboardingComplete

    val drawerRoute = when (currentRoute) {
        Routes.HOME -> Routes.HOME

        Routes.ALARMS,
        Routes.ADD_ALARM,
        Routes.EDIT_ALARM -> Routes.ALARMS

        Routes.REMINDERS,
        Routes.ADD_REMINDER,
        Routes.EDIT_REMINDER,
        Routes.MANAGE_SCHEDULE_CATEGORIES -> Routes.REMINDERS

        Routes.BIRTHDAYS,
        Routes.ADD_BIRTHDAY,
        Routes.EDIT_BIRTHDAY -> Routes.BIRTHDAYS

        Routes.SETTINGS -> Routes.SETTINGS
        Routes.FAMILY_TREE -> Routes.FAMILY_TREE
        Routes.CAPABILITY_SETUP -> {
            if (onboardingComplete) {
                Routes.SETTINGS
            } else {
                null
            }
        }

        Routes.LISTENING -> Routes.LISTENING
        Routes.PROFILE -> Routes.PROFILE

        else -> null
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = onboardingComplete,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawer(
                    currentRoute = drawerRoute,
                    assistantName = assistantName,
                    onNavigate = { route ->
                        scope.launch {
                            drawerState.close()
                        }

                        navController.navigate(route) {
                            popUpTo(Routes.HOME) {
                                saveState = false
                            }

                            launchSingleTop = true
                            restoreState = false
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
                            when {
                                isInitialSetup ->
                                    "Set up $assistantName"

                                drawerRoute == Routes.HOME ->
                                    "Home"

                                drawerRoute == Routes.ALARMS ->
                                    "Alarms"

                                drawerRoute == Routes.REMINDERS ->
                                    "Schedules"

                                drawerRoute == Routes.BIRTHDAYS ->
                                    "Birthdays"

                                drawerRoute == Routes.SETTINGS ->
                                    "Settings"

                                drawerRoute == Routes.LISTENING ->
                                    "Listening"

                                drawerRoute == Routes.PROFILE ->
                                    "My Profile"

                                drawerRoute == Routes.FAMILY_TREE -> "Family Tree"

                                currentRoute == Routes.BACKUP_RESTORE ->
                                    "Backup & Restore"

                                else ->
                                    "$assistantName Assistant"
                            }
                        )
                    },
                    navigationIcon = {
                        if (onboardingComplete) {
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
                    }
                )
            }
        ) { paddingValues ->

            NavHost(
                navController = navController,
                startDestination = if (onboardingComplete) {
                    Routes.HOME
                } else {
                    Routes.CAPABILITY_SETUP
                },
                modifier = Modifier.padding(paddingValues)
            ) {

                // HOME
                composable(Routes.HOME) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModelFactory(
                            alarmRepository =
                                application.alarmRepository,
                            reminderRepository =
                                application.reminderRepository,
                            birthdayRepository =
                                application.birthdayRepository,
                            scheduleRepository =
                                application.scheduleRepository,
                            scheduleCategoryRepository =
                                application.scheduleCategoryRepository
                        )
                    )

                    HomeScreen(
                        viewModel = homeViewModel,
                        assistantName = assistantName,
                        onOpenAlarms = {
                            navController.navigate(Routes.ALARMS)
                        },
                        onOpenReminders = {
                            navController.navigate(Routes.REMINDERS)
                        },
                        onOpenListening = {
                            navController.navigate(Routes.LISTENING)
                        },
                        onOpenBirthdays = {
                            navController.navigate(Routes.BIRTHDAYS)
                        },
                        onOpenSettings = {
                            navController.navigate(Routes.SETTINGS)
                        }
                    )
                }

                // LISTENING
                composable(Routes.LISTENING) {
                    ListeningScreen()
                }

                // SETTINGS
                // Keep only one Settings destination.
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        settingsRepository =
                            application.appSettingsRepository,
                        onBack = {
                            navController.popBackStack()
                        },
                        onOpenCapabilitySetup = {
                            navController.navigate(
                                Routes.CAPABILITY_SETUP
                            )
                        },
                        openBackupRestoreOnLaunch =
                            openBackupRestoreOnSettingsLaunch,
                        onBackupRestoreLaunchHandled = {
                            openBackupRestoreAfterSignIn = false
                        }
                    )
                }

                // ALARMS
                composable(Routes.ALARMS) {
                    val alarmViewModel: AlarmViewModel = viewModel(
                        factory = AlarmViewModelFactory(
                            alarmRepository =
                                application.alarmRepository,
                            alarmService =
                                application.alarmService
                        )
                    )

                    AlarmListScreen(
                        viewModel = alarmViewModel,
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

                // ADD ALARM
                composable(Routes.ADD_ALARM) {
                    val alarmViewModel: AlarmViewModel = viewModel(
                        factory = AlarmViewModelFactory(
                            alarmRepository =
                                application.alarmRepository,
                            alarmService =
                                application.alarmService
                        )
                    )

                    AddAlarmScreen(
                        viewModel = alarmViewModel,
                        onSaved = {
                            navController.popBackStack()
                        },
                        onCancel = {
                            navController.popBackStack()
                        }
                    )
                }

                // EDIT ALARM
                composable(
                    route = Routes.EDIT_ALARM,
                    arguments = listOf(
                        navArgument("alarmId") {
                            type = NavType.LongType
                        }
                    )
                ) { entry ->
                    val alarmId =
                        entry.arguments?.getLong("alarmId")
                            ?: return@composable

                    val alarmViewModel: AlarmViewModel = viewModel(
                        factory = AlarmViewModelFactory(
                            alarmRepository =
                                application.alarmRepository,
                            alarmService =
                                application.alarmService
                        )
                    )

                    val alarms by
                    alarmViewModel.alarms.collectAsState()

                    val alarm = alarms.firstOrNull {
                        it.id == alarmId
                    }

                    if (alarm != null) {
                        EditAlarmScreen(
                            alarm = alarm,
                            viewModel = alarmViewModel,
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

                // SCHEDULES
                composable(Routes.REMINDERS) {
                    val scheduleViewModel: ScheduleViewModel =
                        viewModel(
                            factory = ScheduleViewModelFactory(
                                scheduleRepository =
                                    application.scheduleRepository,
                                categoryRepository =
                                    application.scheduleCategoryRepository,
                                scheduleService =
                                    application.scheduleService
                            )
                        )

                    ScheduleListScreen(
                        viewModel = scheduleViewModel,
                        onAddSchedule = {
                            navController.navigate(
                                Routes.ADD_REMINDER
                            )
                        },
                        onEditSchedule = { scheduleId ->
                            navController.navigate(
                                Routes.editReminder(scheduleId)
                            )
                        },
                        onManageCategories = {
                            navController.navigate(
                                Routes.MANAGE_SCHEDULE_CATEGORIES
                            )
                        }
                    )
                }

                // ADD SCHEDULE
                composable(Routes.ADD_REMINDER) {
                    val scheduleViewModel: ScheduleViewModel =
                        viewModel(
                            factory = ScheduleViewModelFactory(
                                scheduleRepository =
                                    application.scheduleRepository,
                                categoryRepository =
                                    application.scheduleCategoryRepository,
                                scheduleService =
                                    application.scheduleService
                            )
                        )

                    val categories by
                    scheduleViewModel.categories.collectAsState()

                    EditScheduleScreen(
                        viewModel = scheduleViewModel,
                        categories = categories,
                        existingSchedule = null,
                        onSaved = {
                            navController.popBackStack()
                        },
                        onCancel = {
                            navController.popBackStack()
                        }
                    )
                }

                // EDIT SCHEDULE
                composable(
                    route = Routes.EDIT_REMINDER,
                    arguments = listOf(
                        navArgument("scheduleId") {
                            type = NavType.LongType
                        }
                    )
                ) { entry ->
                    val scheduleId =
                        entry.arguments?.getLong("scheduleId")
                            ?: return@composable

                    val scheduleViewModel: ScheduleViewModel =
                        viewModel(
                            factory = ScheduleViewModelFactory(
                                scheduleRepository =
                                    application.scheduleRepository,
                                categoryRepository =
                                    application.scheduleCategoryRepository,
                                scheduleService =
                                    application.scheduleService
                            )
                        )

                    val schedules by
                    scheduleViewModel.schedules.collectAsState()

                    val categories by
                    scheduleViewModel.categories.collectAsState()

                    val schedule = schedules.firstOrNull {
                        it.id == scheduleId
                    }

                    if (schedule != null) {
                        EditScheduleScreen(
                            viewModel = scheduleViewModel,
                            categories = categories,
                            existingSchedule = schedule,
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

                // MANAGE SCHEDULE CATEGORIES
                composable(Routes.MANAGE_SCHEDULE_CATEGORIES) {
                    val scheduleViewModel: ScheduleViewModel =
                        viewModel(
                            factory = ScheduleViewModelFactory(
                                scheduleRepository =
                                    application.scheduleRepository,
                                categoryRepository =
                                    application.scheduleCategoryRepository,
                                scheduleService =
                                    application.scheduleService
                            )
                        )

                    val categories by
                    scheduleViewModel.categories.collectAsState()

                    ManageScheduleCategoriesScreen(
                        viewModel = scheduleViewModel,
                        categories = categories,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // BIRTHDAYS
                composable(Routes.BIRTHDAYS) {
                    val birthdayViewModel: BirthdayViewModel =
                        viewModel(
                            factory = BirthdayViewModelFactory(
                                birthdayRepository =
                                    application.birthdayRepository,
                                birthdayService =
                                    application.birthdayService
                            )
                        )

                    BirthdayListScreen(
                        viewModel = birthdayViewModel,
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

                // ADD BIRTHDAY
                composable(Routes.ADD_BIRTHDAY) {
                    val birthdayViewModel: BirthdayViewModel =
                        viewModel(
                            factory = BirthdayViewModelFactory(
                                birthdayRepository =
                                    application.birthdayRepository,
                                birthdayService =
                                    application.birthdayService
                            )
                        )

                    AddBirthdayScreen(
                        viewModel = birthdayViewModel,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // EDIT BIRTHDAY
                composable(
                    route = Routes.EDIT_BIRTHDAY,
                    arguments = listOf(
                        navArgument("birthdayId") {
                            type = NavType.LongType
                        }
                    )
                ) { entry ->
                    val birthdayId =
                        entry.arguments?.getLong("birthdayId")
                            ?: return@composable

                    val birthdayViewModel: BirthdayViewModel =
                        viewModel(
                            factory = BirthdayViewModelFactory(
                                birthdayRepository =
                                    application.birthdayRepository,
                                birthdayService =
                                    application.birthdayService
                            )
                        )

                    val birthdays by
                    birthdayViewModel.birthdays.collectAsState()

                    val birthday = birthdays.firstOrNull {
                        it.id == birthdayId
                    }

                    if (birthday != null) {
                        EditBirthdayScreen(
                            birthday = birthday,
                            viewModel = birthdayViewModel,
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

                // CAPABILITY SETUP
                composable(Routes.CAPABILITY_SETUP) {
                    CapabilitySetupScreen(
                        isFirstLaunch = !onboardingComplete,
                        settingsRepository =
                            application.appSettingsRepository,
                        onBack = {
                            if (!onboardingComplete) {
                                onboardingPreferences.edit()
                                    .putBoolean(
                                        "capability_setup_completed",
                                        true
                                    )
                                    .apply()

                                onboardingComplete = true

                                navController.navigate(Routes.HOME) {
                                    popUpTo(
                                        Routes.CAPABILITY_SETUP
                                    ) {
                                        inclusive = true
                                    }

                                    launchSingleTop = true
                                }
                            } else {
                                navController.popBackStack()
                            }
                        }
                    )
                }

                // PROFILE
                composable(Routes.PROFILE) {
                    ProfileScreen(
                        onOpenBackupRestore = {
                            navController.navigate(Routes.BACKUP_RESTORE) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                // BACKUP & RESTORE
                composable(Routes.BACKUP_RESTORE) {
                    BackupRestoreScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(Routes.FAMILY_TREE) {
                    FamilyTreeScreen()
                }
            }
        }
    }
}