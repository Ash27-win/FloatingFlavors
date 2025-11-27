package com.example.floatingflavors.app.core.navigation

// core/navigation/Screen.kt

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")

    data object Login : Screen("login")

    data object Register : Screen("register")
    // Role based destinations
    data object UserHome : Screen("user_home")
    data object AdminHome : Screen("admin_home")
}
