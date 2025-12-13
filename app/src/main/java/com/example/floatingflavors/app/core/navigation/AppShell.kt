package com.example.floatingflavors.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * AppShell - Bottom navigation wired to the Screen routes used by AppNavHost.
 *
 * IMPORTANT: This uses the same route strings from your Screen sealed class.
 * Do NOT use different routes like "dashboard" unless AppNavHost also registers them.
 */

private sealed class BottomTab(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object AdminDashboard : BottomTab(Screen.AdminDashboard.route, "Dashboard", { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") })
    object Orders : BottomTab(Screen.AdminMenuInventory.route, "Menu", { Icon(Icons.Default.List, contentDescription = "Orders/Menu") })
    object Menu : BottomTab(Screen.UserMenuGrid.route, "Browse", { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") })
    object Alerts : BottomTab(Screen.AdminDashboard.route, "Alerts", { Icon(Icons.Default.Notifications, contentDescription = "Alerts") }) // replace if you add a dedicated route
    object Profile : BottomTab(Screen.UserProfile.route, "Profile", { Icon(Icons.Default.Person, contentDescription = "Profile") })
}

@Composable
fun AppShell(startRoute: String = Screen.AdminDashboard.route) {
    val navController = rememberNavController()

    val tabs = listOf(
        BottomTab.AdminDashboard,
        BottomTab.Orders,
        BottomTab.Menu,
        BottomTab.Alerts,
        BottomTab.Profile
    )

    Scaffold(
        bottomBar = {
            val back by navController.currentBackStackEntryAsState()
            val current = back?.destination?.route
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = (current == tab.route),
                        onClick = {
                            // navigate safely: popUpTo startDestination to avoid multiple copies
                            navController.navigate(tab.route) {
                                launchSingleTop = true
                                // keep navigation simple — don't popUpTo aggressively here
                            }
                        },
                        icon = tab.icon,
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Forward padding to AppNavHost so content sits above bottomBar
        AppNavHost(navController = navController, startDestination = startRoute, modifier = Modifier.padding(innerPadding))
    }
}