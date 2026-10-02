
package com.aalavandaanus.kabaliassistant.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aalavandaanus.kabaliassistant.features.birthdays.AddBirthdayScreen
import com.aalavandaanus.kabaliassistant.features.birthdays.BirthdayListScreen
import com.aalavandaanus.kabaliassistant.features.home.HomeScreen
import com.aalavandaanus.kabaliassistant.features.listening.ListeningScreen
import com.aalavandaanus.kabaliassistant.ui.components.KabaliTopBar
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val drawerState = rememberDrawerState(
        initialValue = DrawerValue.Closed
    )

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,

        drawerContent = {

            NavigationDrawer(
                currentRoute = navController
                    .currentBackStackEntry
                    ?.destination
                    ?.route,

                onNavigate = { route ->

                    scope.launch {
                        drawerState.close()
                    }

                    navController.navigate(route) {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) {

        Scaffold(

            topBar = {

                KabaliTopBar(

                    title = navController
                        .currentBackStackEntry
                        ?.destination
                        ?.route
                        ?.let { route ->

                            when {

                                route == AppRoute.Home.route ->
                                    AppRoute.Home.title

                                route == AppRoute.Listening.route ->
                                    AppRoute.Listening.title

                                route == AppRoute.Birthdays.route ->
                                    AppRoute.Birthdays.title

                                route == AppRoute.AddBirthday.route ->
                                    AppRoute.AddBirthday.title

                                route.startsWith("editBirthday/") ->
                                    "Edit Birthday"

                                else ->
                                    "Kabali Assistant"
                            }
                        }
                        ?: "Kabali Assistant",

                    onMenuClick = {

                        scope.launch {
                            drawerState.open()
                        }
                    }
                )
            }
        ) { innerPadding ->

            NavHost(

                navController = navController,

                startDestination =
                    AppRoute.Home.route,

                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {

                // ====================================================
                // HOME
                // ====================================================

                composable(
                    AppRoute.Home.route
                ) {

                    HomeScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // ====================================================
                // LISTENING
                // ====================================================

                composable(
                    AppRoute.Listening.route
                ) {

                    ListeningScreen(
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }

                // ====================================================
                // BIRTHDAYS
                // ====================================================

                composable(
                    AppRoute.Birthdays.route
                ) {

                    BirthdayListScreen(

                        onAddBirthday = {

                            navController.navigate(
                                AppRoute.AddBirthday.route
                            )
                        },

                        onEditBirthday = { birthdayId ->

                            navController.navigate(
                                "editBirthday/$birthdayId"
                            )
                        },

                        onBack = {

                            navController.popBackStack()
                        }
                    )
                }

                // ====================================================
                // ADD BIRTHDAY
                // ====================================================

                composable(
                    AppRoute.AddBirthday.route
                ) {

                    AddBirthdayScreen(

                        birthdayId = null,

                        onBack = {

                            navController.popBackStack()
                        }
                    )
                }

                // ====================================================
                // EDIT BIRTHDAY
                // ====================================================

                composable(
                    route = "editBirthday/{birthdayId}",

                    arguments = listOf(
                        navArgument("birthdayId") {
                            type = NavType.IntType
                        }
                    )
                ) { backStackEntry ->

                    val birthdayId =
                        backStackEntry
                            .arguments
                            ?.getInt("birthdayId")

                    AddBirthdayScreen(

                        birthdayId = birthdayId,

                        onBack = {

                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
