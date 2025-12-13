package com.example.floatingflavors.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.RestaurantMenu
// Removed unused import: import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.floatingflavors.app.feature.user.presentation.UserHomeScreen
// --- FIX: ADDED MISSING IMPORTS ---
import com.example.floatingflavors.app.feature.user.presentation.menu.UserMenuGridScreen

@Composable
fun UserShell(startRoute: String = Screen.UserHome.route) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val back by navController.currentBackStackEntryAsState()
            val current = back?.destination?.route
            NavigationBar {
                NavigationBarItem(
                    selected = current == Screen.UserHome.route,
                    onClick = { navController.navigate(Screen.UserHome.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = current == Screen.UserMenuGrid.route,
                    onClick = { navController.navigate(Screen.UserMenuGrid.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
                    label = { Text("Menu") }
                )
                NavigationBarItem(
                    selected = current == Screen.UserProfile.route,
                    onClick = { navController.navigate(Screen.UserProfile.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = startRoute, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.UserHome.route) { UserHomeScreen(onBrowseMenu = { navController.navigate(Screen.UserMenuGrid.route) }) }
            composable(Screen.UserMenuGrid.route) { UserMenuGridScreen(onItemClick = { /* show details */ }) }
            composable(Screen.UserProfile.route) { /* TODO: user profile screen */ UserHomeScreen(onBrowseMenu = {}) }
        }
    }
}