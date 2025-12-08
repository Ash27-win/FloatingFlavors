package com.example.floatingflavors.app.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.floatingflavors.app.feature.admin.presentation.dashboard.AdminDashboardScreen
import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminMenuInventoryScreen
import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminAddFoodScreen
import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminEditFoodScreen

// IMPORT: real Admin Orders screen (replace placeholder)
import com.example.floatingflavors.app.feature.admin.presentation.orders.AdminOrdersScreen

@Composable
fun AdminShell(startRoute: String = Screen.AdminDashboard.route) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val back by navController.currentBackStackEntryAsState()
            val current = back?.destination?.route
            NavigationBar {
                NavigationBarItem(
                    selected = current == Screen.AdminDashboard.route,
                    onClick = { navController.navigate(Screen.AdminDashboard.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") }
                )

                NavigationBarItem(
                    selected = current == Screen.AdminOrders.route,
                    onClick = { navController.navigate(Screen.AdminOrders.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Orders") },
                    label = { Text("Orders") }
                )

                NavigationBarItem(
                    selected = current == Screen.AdminMenuInventory.route,
                    onClick = { navController.navigate(Screen.AdminMenuInventory.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
                    label = { Text("Menu") }
                )

                NavigationBarItem(
                    selected = current == Screen.AdminNotifications.route,
                    onClick = { navController.navigate(Screen.AdminNotifications.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
                    label = { Text("Alerts") }
                )

                NavigationBarItem(
                    selected = current == Screen.AdminProfile.route,
                    onClick = { navController.navigate(Screen.AdminProfile.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = startRoute, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.AdminDashboard.route) { AdminDashboardScreen() }

            // Use the real AdminOrdersScreen (replaces placeholder)
            composable(Screen.AdminOrders.route) { AdminOrdersScreen() }

            // Menu + Inventory (your existing screen). It expects navController for add/edit navigation.
            composable(Screen.AdminMenuInventory.route) { AdminMenuInventoryScreen(navController = navController) }

            // Add Food can be a separate route (we will navigate to it from Menu via FAB)
            composable(Screen.AdminAddFood.route) { AdminAddFoodScreen(navController = navController) }

            composable("admin_edit_food/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                AdminEditFoodScreen(
                    navController = navController,
                    itemId = id
                )
            }

            // Notifications route
            composable(Screen.AdminNotifications.route) { AdminNotificationsPlaceholder() }

            // Profile route
            composable(Screen.AdminProfile.route) { AdminProfilePlaceholder() }
        }
    }
}

/** Small lightweight placeholders so the app won't crash if you don't have these screens yet.
 * Replace these with your real implementations (AdminNotificationsScreen, AdminProfileScreen) when ready.
 */
@Composable
private fun AdminNotificationsPlaceholder() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text("Notifications (placeholder)", modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun AdminProfilePlaceholder() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text("Admin Profile (placeholder)", modifier = Modifier.padding(16.dp))
    }
}
