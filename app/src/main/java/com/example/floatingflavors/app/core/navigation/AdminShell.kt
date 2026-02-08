package com.example.floatingflavors.app.core.navigation

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.example.floatingflavors.app.feature.admin.presentation.dashboard.AdminDashboardScreen
import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminMenuInventoryScreen
import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminAddFoodScreen
import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminEditFoodScreen
import com.example.floatingflavors.app.feature.admin.presentation.orders.AdminOrdersScreen
import com.example.floatingflavors.app.feature.admin.presentation.notification.AdminNotificationScreen

// admin settings wiring
import com.example.floatingflavors.app.feature.admin.data.remote.AdminSettingsRepository
import com.example.floatingflavors.app.feature.admin.data.remote.AdminSettingsApi
import com.example.floatingflavors.app.feature.admin.presentation.settings.AdminSettingsViewModel
import com.example.floatingflavors.app.feature.admin.presentation.settings.AdminSettingsScreen
import com.example.floatingflavors.app.core.network.NetworkClient

@Composable
fun AdminShell(rootNavController: NavHostController, startRoute: String = Screen.AdminDashboard.route) {
    val navController = rememberNavController()

    // 🔔 NOTIFICATION INTENT HANDLER (Live & Pending)
    LaunchedEffect(Unit) {
        // 1. Check Pending (Cold Start)
        if (com.example.floatingflavors.app.core.navigation.PendingNotification.hasPending()) {
            val pending = com.example.floatingflavors.app.core.navigation.PendingNotification.consume()
            pending?.let { (screen, refId) ->
                 if (screen == "AdminOrderDetails" || screen == "OrderTracking") {
                     // If refId is needed, navigate with argument
                     navController.navigate(Screen.AdminOrders.route) 
                     // TODO: In future pass refId deep to order details
                 }
            }
        }
        
        // 2. Check Live Events (Hot Start)
        com.example.floatingflavors.app.core.service.NotificationEventBus.events.collect { event ->
            if (event is com.example.floatingflavors.app.core.service.NotificationEvent.Navigate) {
                 if (event.screen == "AdminOrderDetails" && !event.referenceId.isNullOrEmpty()) {
                     navController.navigate(Screen.AdminOrders.createRoute(event.referenceId))
                 } else if (event.screen == "OrderTracking") { // fallback
                     navController.navigate(Screen.AdminOrders.createRoute()) 
                 }
            }
        }
    }

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
                    selected = current?.startsWith("admin_orders") == true,
                    onClick = { navController.navigate(Screen.AdminOrders.createRoute()) { launchSingleTop = true } },
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
                    selected = current == Screen.AdminUserList.route,
                    onClick = { navController.navigate(Screen.AdminUserList.route) { launchSingleTop = true } },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Users") },
                    label = { Text("Users") }
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
            composable(Screen.AdminDashboard.route) { 
                AdminDashboardScreen(
                    onNotificationClick = { navController.navigate(Screen.AdminNotifications.route) }
                ) 
            }

            // Use the real AdminOrdersScreen (replaces placeholder)
            composable(
                route = Screen.AdminOrders.route,
                arguments = listOf(androidx.navigation.navArgument("orderId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                // Parse optional arguments
                val orderIdArg = backStackEntry.arguments?.getString("orderId")
                AdminOrdersScreen(openOrderId = orderIdArg?.toIntOrNull()) 
            }

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
            composable(Screen.AdminNotifications.route) {
                AdminNotificationScreen()
            }
            
            // [NEW] User/Delivery List
            composable(Screen.AdminUserList.route) {
                com.example.floatingflavors.app.feature.admin.presentation.users.AdminUserListScreen(navController)
            }
            
            // [NEW] User Details
            composable("admin_user_details/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: "0"
                com.example.floatingflavors.app.feature.admin.presentation.users.AdminUserDetailsScreen(navController, userId)
            }

            // Profile route — create repo + vm + screen
            composable(Screen.AdminProfile.route) {
                /**
                 * Create API/repo/factory/VM normally (these are not long-running IO ops).
                 * Any network call (vm.load) is invoked in LaunchedEffect with try/catch.
                 * We do NOT wrap composable invocation in try/catch.
                 */
                val api: AdminSettingsApi = NetworkClient.retrofit.create(AdminSettingsApi::class.java)
                val repo = AdminSettingsRepository(api)
                val factory = object : ViewModelProvider.Factory {
                    @Suppress("UNCHECKED_CAST")
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return AdminSettingsViewModel(repo) as T
                    }
                }
                val vm: AdminSettingsViewModel = viewModel(factory = factory)

                // async load with error handling
                LaunchedEffect(Unit) {
                    try {
                        vm.load(adminId = com.example.floatingflavors.app.core.UserSession.userId)
                        Log.d("AdminShell", "Requested AdminSettings load")
                    } catch (e: Exception) {
                        Log.e("AdminShell", "Failed to load AdminSettings", e)
                    }
                }

                AdminSettingsScreen(
                    viewModel = vm,
                    rootNavController = rootNavController,
                    onSignOut = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.AdminRoot.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

/** Small lightweight placeholders so the app won't crash if you don't have these screens yet.
 * Replace these with your real implementations (AdminNotificationsScreen, AdminProfileScreen) when ready.
 */

@Composable
private fun AdminProfilePlaceholder() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Text("Admin Profile (placeholder)", modifier = Modifier.padding(16.dp))
    }
}




//package com.example.floatingflavors.app.core.navigation
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Dashboard
//import androidx.compose.material.icons.filled.ListAlt
//import androidx.compose.material.icons.filled.RestaurantMenu
//import androidx.compose.material.icons.filled.Notifications
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.foundation.layout.padding
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import com.example.floatingflavors.app.feature.admin.presentation.dashboard.AdminDashboardScreen
//import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminMenuInventoryScreen
//import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminAddFoodScreen
//import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminEditFoodScreen
//
//// IMPORT: real Admin Orders screen (replace placeholder)
//import com.example.floatingflavors.app.feature.admin.presentation.orders.AdminOrdersScreen
//
//@Composable
//fun AdminShell(startRoute: String = Screen.AdminDashboard.route) {
//    val navController = rememberNavController()
//
//    Scaffold(
//        bottomBar = {
//            val back by navController.currentBackStackEntryAsState()
//            val current = back?.destination?.route
//            NavigationBar {
//                NavigationBarItem(
//                    selected = current == Screen.AdminDashboard.route,
//                    onClick = { navController.navigate(Screen.AdminDashboard.route) { launchSingleTop = true } },
//                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
//                    label = { Text("Dashboard") }
//                )
//
//                NavigationBarItem(
//                    selected = current == Screen.AdminOrders.route,
//                    onClick = { navController.navigate(Screen.AdminOrders.route) { launchSingleTop = true } },
//                    icon = { Icon(Icons.Default.ListAlt, contentDescription = "Orders") },
//                    label = { Text("Orders") }
//                )
//
//                NavigationBarItem(
//                    selected = current == Screen.AdminMenuInventory.route,
//                    onClick = { navController.navigate(Screen.AdminMenuInventory.route) { launchSingleTop = true } },
//                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
//                    label = { Text("Menu") }
//                )
//
//                NavigationBarItem(
//                    selected = current == Screen.AdminNotifications.route,
//                    onClick = { navController.navigate(Screen.AdminNotifications.route) { launchSingleTop = true } },
//                    icon = { Icon(Icons.Default.Notifications, contentDescription = "Notifications") },
//                    label = { Text("Alerts") }
//                )
//
//                NavigationBarItem(
//                    selected = current == Screen.AdminProfile.route,
//                    onClick = { navController.navigate(Screen.AdminProfile.route) { launchSingleTop = true } },
//                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
//                    label = { Text("Profile") }
//                )
//            }
//        }
//    ) { innerPadding ->
//        NavHost(navController = navController, startDestination = startRoute, modifier = Modifier.padding(innerPadding)) {
//            composable(Screen.AdminDashboard.route) { AdminDashboardScreen() }
//
//            // Use the real AdminOrdersScreen (replaces placeholder)
//            composable(Screen.AdminOrders.route) { AdminOrdersScreen() }
//
//            // Menu + Inventory (your existing screen). It expects navController for add/edit navigation.
//            composable(Screen.AdminMenuInventory.route) { AdminMenuInventoryScreen(navController = navController) }
//
//            // Add Food can be a separate route (we will navigate to it from Menu via FAB)
//            composable(Screen.AdminAddFood.route) { AdminAddFoodScreen(navController = navController) }
//
//            composable("admin_edit_food/{id}") { backStackEntry ->
//                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
//                AdminEditFoodScreen(
//                    navController = navController,
//                    itemId = id
//                )
//            }
//
//            // Notifications route
//            composable(Screen.AdminNotifications.route) { AdminNotificationsPlaceholder() }
//
//            // Profile route
//            composable(Screen.AdminProfile.route) { AdminProfilePlaceholder() }
//        }
//    }
//}
//
///** Small lightweight placeholders so the app won't crash if you don't have these screens yet.
// * Replace these with your real implementations (AdminNotificationsScreen, AdminProfileScreen) when ready.
// */
//@Composable
//private fun AdminNotificationsPlaceholder() {
//    Surface(modifier = Modifier.fillMaxSize()) {
//        Text("Notifications (placeholder)", modifier = Modifier.padding(16.dp))
//    }
//}
//
//@Composable
//private fun AdminProfilePlaceholder() {
//    Surface(modifier = Modifier.fillMaxSize()) {
//        Text("Admin Profile (placeholder)", modifier = Modifier.padding(16.dp))
//    }
//}
