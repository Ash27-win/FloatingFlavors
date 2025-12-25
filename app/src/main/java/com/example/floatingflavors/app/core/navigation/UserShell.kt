package com.example.floatingflavors.app.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.user.data.booking.BookingRepository
import com.example.floatingflavors.app.feature.user.data.settings.*
import com.example.floatingflavors.app.feature.user.presentation.UserHomeScreen
import com.example.floatingflavors.app.feature.user.presentation.booking.BookingScreen
import com.example.floatingflavors.app.feature.user.presentation.booking.BookingViewModel
import com.example.floatingflavors.app.feature.user.presentation.booking.EventMenuScreen
import com.example.floatingflavors.app.feature.user.presentation.membership.MembershipScreen
import com.example.floatingflavors.app.feature.user.presentation.menu.UserMenuGridScreen
import com.example.floatingflavors.app.feature.user.presentation.settings.*
import com.example.floatingflavors.app.feature.user.presentation.settings.edit.*
import com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress.*
import kotlinx.coroutines.launch

@Composable
fun UserShell(
    startRoute: String = Screen.UserHome.route
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // 🔥 SINGLE SOURCE OF TRUTH
    val addressViewModel = remember {
        AddressViewModel(AddressRepository(NetworkClient.addressApi))
    }

    val editAddressViewModel = remember {
        EditAddressViewModel(AddressRepository(NetworkClient.addressApi))
    }

    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = currentRoute == Screen.UserHome.route,
                    onClick = {
                        navController.navigate(Screen.UserHome.route) {
                            popUpTo(Screen.UserHome.route)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.UserMenuGrid.route,
                    onClick = {
                        navController.navigate(Screen.UserMenuGrid.route) {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.RestaurantMenu, null) },
                    label = { Text("Menu") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.UserBooking.route,
                    onClick = {
                        navController.navigate(Screen.UserBooking.route)
                    },
                    icon = { Icon(Icons.Default.EventAvailable, null) },
                    label = { Text("Booking") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.UserOrders.route,
                    onClick = {
                        navController.navigate(Screen.UserOrders.route)
                    },
                    icon = { Icon(Icons.Default.ReceiptLong, null) },
                    label = { Text("Orders") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.UserProfile.route,
                    onClick = {
                        navController.navigate(Screen.UserProfile.route)
                    },
                    icon = { Icon(Icons.Default.Settings, null) },
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

            composable(Screen.UserHome.route) {
                UserHomeScreen(
                    onBrowseMenu = {
                        navController.navigate(Screen.UserMenuGrid.route)
                    },
                    onOpenMembership = {
                        navController.navigate(Screen.UserMembership.route)
                    }
                )
            }

            composable(Screen.UserMenuGrid.route) {
                UserMenuGridScreen() // untouched
            }

            composable(Screen.UserBooking.route) {
                val bookingVm = remember {
                    BookingViewModel(
                        BookingRepository(NetworkClient.bookingApi)
                    )
                }

                val snackbarHostState = remember { SnackbarHostState() }
                val coroutineScope = rememberCoroutineScope()

                BookingScreen(
                    vm = bookingVm,
                    userId = 1,
                    onNavigateToMenu = { bookingId ->
                        navController.navigate(
                            Screen.UserBookingMenu.createRoute(bookingId.toIntOrNull() ?: 0)
                        )
                    },
                    onShowMessage = { message ->
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(message)
                        }
                    }
                )

                // Add Snackbar host
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(16.dp)
                )
            }
            composable(
                route = Screen.UserBookingMenu.route,
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.IntType }
                )
            ) { entry ->
                val bookingId = entry.arguments!!.getInt("bookingId")

                EventMenuScreen(
                    bookingId = bookingId,
                    onBack = { navController.popBackStack() }
                )
            }


            composable(Screen.UserOrders.route) {
                Text("Orders & Tracking Screen")
            }

            composable(Screen.UserProfile.route) {

                val settingsVm = remember {
                    SettingsViewModel(
                        UserSettingsRepository(NetworkClient.userSettingsApi)
                    )
                }

                SettingsScreen(
                    viewModel = settingsVm,
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

            composable(Screen.EditProfile.route) {

                val editProfileVm = remember {
                    EditProfileViewModel(
                        EditProfileRepository(NetworkClient.editProfileApi)
                    )
                }

                EditProfileScreen(
                    viewModel = editProfileVm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TermsOfService.route) {
                TermsOfServiceScreen { navController.popBackStack() }
            }

            composable(Screen.PrivacyPolicy.route) {
                PrivacyPolicyScreen { navController.popBackStack() }
            }

            composable(Screen.SavedAddresses.route) {
                SavedAddressScreen(
                    vm = addressViewModel,
                    userId = 1,
                    onBack = { navController.popBackStack() },
                    onAdd = {
                        navController.navigate(Screen.AddAddress.route)
                    },
                    onEdit = { addressId ->
                        navController.navigate(
                            Screen.EditAddress.createRoute(addressId)
                        )
                    }
                )
            }

            composable(Screen.AddAddress.route) {
                AddAddressScreen(
                    vm = addressViewModel,
                    userId = 1,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditAddress.route,
                arguments = listOf(navArgument("addressId") {
                    type = NavType.IntType
                })
            ) { entry ->
                val addressId = entry.arguments!!.getInt("addressId")

                val address = addressViewModel.addresses
                    .first { it.id == addressId }

                EditAddressScreen(
                    vm = editAddressViewModel,
                    address = address,
                    userId = 1,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.UserMembership.route) {
                MembershipScreen(navController)
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