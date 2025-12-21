package com.example.floatingflavors.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.user.data.settings.EditProfileRepository
import com.example.floatingflavors.app.feature.user.data.settings.UserSettingsRepository
import com.example.floatingflavors.app.feature.user.presentation.UserHomeScreen
import com.example.floatingflavors.app.feature.user.presentation.menu.UserMenuGridScreen
import com.example.floatingflavors.app.feature.user.presentation.settings.edit.EditProfileScreen
import com.example.floatingflavors.app.feature.user.presentation.settings.edit.EditProfileViewModel
import com.example.floatingflavors.app.feature.user.presentation.settings.PrivacyPolicyScreen
import com.example.floatingflavors.app.feature.user.presentation.settings.SettingsViewModel
import com.example.floatingflavors.app.feature.user.presentation.settings.SettingsScreen
import com.example.floatingflavors.app.feature.user.presentation.settings.TermsOfServiceScreen
import com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress.SavedAddressScreen
import com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress.AddAddressScreen
import com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress.AddressViewModel
import com.example.floatingflavors.app.feature.user.data.settings.AddressRepository


@Composable
fun UserShell(
    startRoute: String = Screen.UserHome.route
) {

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {

                // 🔹 HOME
                NavigationBarItem(
                    selected = currentRoute == Screen.UserHome.route,
                    onClick = {
                        navController.navigate(Screen.UserHome.route) {
                            popUpTo(Screen.UserHome.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )

                // 🔹 MENU
                NavigationBarItem(
                    selected = currentRoute == Screen.UserMenuGrid.route,
                    onClick = {
                        navController.navigate(Screen.UserMenuGrid.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
                    label = { Text("Menu") }
                )

                // 🔹 BOOKING
                NavigationBarItem(
                    selected = currentRoute == Screen.UserBooking.route,
                    onClick = {
                        navController.navigate(Screen.UserBooking.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = "Booking") },
                    label = { Text("Booking") }
                )

                // 🔹 ORDERS / TRACK
                NavigationBarItem(
                    selected = currentRoute == Screen.UserOrders.route,
                    onClick = {
                        navController.navigate(Screen.UserOrders.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                    label = { Text("Orders") }
                )

                // 🔹 PROFILE
                NavigationBarItem(
                    selected = currentRoute == Screen.UserProfile.route,
                    onClick = {
                        navController.navigate(Screen.UserProfile.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") }
                )

            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(padding)
        ) {

            // ✅ HOME
            composable(Screen.UserHome.route) {
                UserHomeScreen(
                    onBrowseMenu = {
                        navController.navigate(Screen.UserMenuGrid.route)
                    }
                )
            }

            // ✅ MENU
            composable(Screen.UserMenuGrid.route) {
                UserMenuGridScreen()
            }

            // ✅ BOOKING (Placeholder – UI next)
            composable(Screen.UserBooking.route) {
                Text("Booking Screen")
            }

            // ✅ ORDERS / TRACK (Placeholder – UI next)
            composable(Screen.UserOrders.route) {
                Text("Orders & Tracking Screen")
            }

            // 🔹 SETTINGS
            composable(Screen.UserProfile.route) {

                val viewModel = remember {
                    SettingsViewModel(
                        UserSettingsRepository(
                            NetworkClient.userSettingsApi
                        )
                    )
                }
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onEditProfileClick = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateTerms = {
                        navController.navigate(Screen.TermsOfService.route)
                    },
                    onNavigatePrivacy = {
                        navController.navigate(Screen.PrivacyPolicy.route)
                    },
                    onSavedAddressClick = {
                        navController.navigate(Screen.SavedAddresses.route)
                    }
                )
            }

            composable(Screen.TermsOfService.route) {
                TermsOfServiceScreen { navController.popBackStack() }
            }

            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen { navController.popBackStack() }
            }

            composable(Screen.EditProfile.route) {

                val editProfileViewModel = remember {
                    EditProfileViewModel(
                        EditProfileRepository(NetworkClient.editProfileApi)
                    )
                }

                EditProfileScreen(
                    viewModel = editProfileViewModel,
                    onBack = { navController.popBackStack() }
                )
            }

            // 🔹 SAVED ADDRESSES
            composable(Screen.SavedAddresses.route) {

                val vm = remember {
                    AddressViewModel(
                        AddressRepository(NetworkClient.addressApi)
                    )
                }

                SavedAddressScreen(
                    vm = vm,
                    userId = 1, // TEMP (UserSession later)
                    onBack = { navController.popBackStack() },
                    onAdd = {
                        navController.navigate(Screen.AddAddress.route)
                    }
                )
            }

// 🔹 ADD ADDRESS
            composable(Screen.AddAddress.route) {

                val vm = remember {
                    AddressViewModel(
                        AddressRepository(NetworkClient.addressApi)
                    )
                }

                AddAddressScreen(
                    vm = vm,
                    userId = 1, // TEMP
                    onBack = { navController.popBackStack() }
                )
            }

        }
    }
}





//package com.example.floatingflavors.app.core.navigation
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Home
//import androidx.compose.material.icons.filled.RestaurantMenu
//// Removed unused import: import androidx.compose.material.icons.filled.Notifications
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.foundation.layout.padding
//import androidx.compose.ui.Modifier
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.compose.currentBackStackEntryAsState
//import androidx.navigation.compose.rememberNavController
//import com.example.floatingflavors.app.feature.user.presentation.UserHomeScreen
//// --- FIX: ADDED MISSING IMPORTS ---
//import com.example.floatingflavors.app.feature.user.presentation.menu.UserMenuGridScreen
//
//@Composable
//fun UserShell(startRoute: String = Screen.UserHome.route) {
//    val navController = rememberNavController()
//
//    Scaffold(
//        bottomBar = {
//            val back by navController.currentBackStackEntryAsState()
//            val current = back?.destination?.route
//            NavigationBar {
//                NavigationBarItem(
//                    selected = current == Screen.UserHome.route,
//                    onClick = { navController.navigate(Screen.UserHome.route) { launchSingleTop = true } },
//                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
//                    label = { Text("Home") }
//                )
//                NavigationBarItem(
//                    selected = current == Screen.UserMenuGrid.route,
//                    onClick = { navController.navigate(Screen.UserMenuGrid.route) { launchSingleTop = true } },
//                    icon = { Icon(Icons.Default.RestaurantMenu, contentDescription = "Menu") },
//                    label = { Text("Menu") }
//                )
//                NavigationBarItem(
//                    selected = current == Screen.UserProfile.route,
//                    onClick = { navController.navigate(Screen.UserProfile.route) { launchSingleTop = true } },
//                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
//                    label = { Text("Profile") }
//                )
//            }
//        }
//    ) { innerPadding ->
//        NavHost(navController = navController, startDestination = startRoute, modifier = Modifier.padding(innerPadding)) {
//            composable(Screen.UserHome.route) { UserHomeScreen(onBrowseMenu = { navController.navigate(Screen.UserMenuGrid.route) }) }
//            composable(Screen.UserMenuGrid.route) { UserMenuGridScreen(onItemClick = { /* show details */ }) }
//            composable(Screen.UserProfile.route) { /* TODO: user profile screen */ UserHomeScreen(onBrowseMenu = {}) }
//        }
//    }
//}