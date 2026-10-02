package com.aalavandaanus.kabaliassistant.navigation

sealed class AppRoute(
    val route: String,
    val title: String
) {
    data object Home : AppRoute("home", "Home")
    data object Listening : AppRoute("listening", "Listening")
    data object Birthdays : AppRoute("birthdays", "Birthdays")
    data object AddBirthday : AppRoute("add_birthday", "Add Birthday")
}