package com.example.floatingflavors.app.core.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.floatingflavors.app.core.navigation.Screen
import com.example.floatingflavors.app.feature.delivery.presentation.DeliveryDashboardScreen
import com.example.floatingflavors.app.feature.delivery.presentation.DeliveryProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.presentation.DeliveryDashboardViewModel
import com.example.floatingflavors.app.feature.delivery.presentation.DeliveryDashboardViewModelFactory
import com.example.floatingflavors.app.feature.delivery.presentation.DeliveryOrderDetailsScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeliveryShell(
    startRoute: String = Screen.DeliveryDashboard.route
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Screen.DeliveryDashboard.route,
                    onClick = {
                        navController.navigate(Screen.DeliveryDashboard.route) {
                            popUpTo(Screen.DeliveryDashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(Icons.Default.DirectionsBike, contentDescription = "Orders")
                    },
                    label = { Text("Orders") }
                )

                NavigationBarItem(
                    selected = currentRoute == Screen.DeliveryProfile.route,
                    onClick = {
                        navController.navigate(Screen.DeliveryProfile.route) {
                            popUpTo(Screen.DeliveryDashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    icon = {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.DeliveryDashboard.route) {
                val deliveryApi = NetworkClient.deliveryApi
                val repository = DeliveryRepository(deliveryApi)
                val deliveryPartnerId = 4 // from login response

                val factory = DeliveryDashboardViewModelFactory(
                    repository = repository,
                    deliveryPartnerId = deliveryPartnerId
                )

                val dashboardViewModel: DeliveryDashboardViewModel =
                    viewModel(factory = factory)

                DeliveryDashboardScreen(
                    viewModel = dashboardViewModel,
                    onViewDetails = { orderId ->
                        navController.navigate(
                            Screen.DeliveryOrderDetails.createRoute(orderId)
                        )
                    }
                )


//                DeliveryDashboardScreen(
//                    viewModel = dashboardViewModel,
//                    deliveryPartnerId = deliveryPartnerId,
//                    onViewDetails = { orderId ->
//                        // ✅ Navigate to order details with GPS
//                        navController.navigate("delivery_order_details/$orderId")
//                    }
//                )
            }

            // ✅ ADD NEW ROUTE for Delivery Order Details
            composable(
                route = "delivery_order_details/{orderId}",
                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
            ) { backStackEntry ->
                val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
                val deliveryPartnerId = 4 // Get from login/session

                DeliveryOrderDetailsScreen(
                    orderId = orderId,
                    deliveryPartnerId = deliveryPartnerId,
                    onBack = {
                        // ✅ Handle back navigation
                        navController.navigateUp()
                    }
                )
            }

            composable(Screen.DeliveryProfile.route) {
                DeliveryProfileScreen()
            }
        }
    }
}





//package com.example.floatingflavors.app.core.navigation
//
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.DirectionsBike
//import androidx.compose.material.icons.filled.Person
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.foundation.layout.padding
//import androidx.compose.ui.Modifier
//import androidx.navigation.compose.*
//import com.example.floatingflavors.app.core.navigation.Screen
//import com.example.floatingflavors.app.feature.delivery.presentation.DeliveryDashboardScreen
//import com.example.floatingflavors.app.feature.delivery.presentation.DeliveryProfileScreen
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.floatingflavors.app.core.network.NetworkClient
//import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
//import com.example.floatingflavors.app.feature.delivery.presentation.DeliveryDashboardViewModel
//import com.example.floatingflavors.app.feature.delivery.presentation.DeliveryDashboardViewModelFactory
//
//@Composable
//fun DeliveryShell(
//    startRoute: String = Screen.DeliveryDashboard.route
//) {
//    val navController = rememberNavController()
//    val backStack by navController.currentBackStackEntryAsState()
//    val currentRoute = backStack?.destination?.route
//
//    Scaffold(
//        bottomBar = {
//            NavigationBar {
//
//                NavigationBarItem(
//                    selected = currentRoute == Screen.DeliveryDashboard.route,
//                    onClick = {
//                        navController.navigate(Screen.DeliveryDashboard.route) {
//                            launchSingleTop = true
//                        }
//                    },
//                    icon = {
//                        Icon(Icons.Default.DirectionsBike, contentDescription = "Orders")
//                    },
//                    label = { Text("Orders") }
//                )
//
//                NavigationBarItem(
//                    selected = currentRoute == Screen.DeliveryProfile.route,
//                    onClick = {
//                        navController.navigate(Screen.DeliveryProfile.route) {
//                            launchSingleTop = true
//                        }
//                    },
//                    icon = {
//                        Icon(Icons.Default.Person, contentDescription = "Profile")
//                    },
//                    label = { Text("Profile") }
//                )
//            }
//        }
//    ) { padding ->
//
//        NavHost(
//            navController = navController,
//            startDestination = startRoute,
//            modifier = Modifier.padding(padding)
//        ) {
//
//            composable(Screen.DeliveryDashboard.route) {
//
//                val deliveryApi = NetworkClient.deliveryApi
//                val repository = DeliveryRepository(deliveryApi)
//
//                val deliveryPartnerId = 4 // from login response
//
//                val factory = DeliveryDashboardViewModelFactory(
//                    repository = repository,
//                    deliveryPartnerId = deliveryPartnerId
//                )
//
//                val dashboardViewModel: DeliveryDashboardViewModel =
//                    viewModel(factory = factory)
//
//                DeliveryDashboardScreen(
//                    viewModel = dashboardViewModel,
//                    deliveryPartnerId = deliveryPartnerId,
//                    onViewDetails = { orderId ->
//                        // later
//                    }
//                )
//            }
//
//
//
//
//            composable(Screen.DeliveryProfile.route) {
//                DeliveryProfileScreen(
//                    onLogout = {
//                        // handle logout later
//                    }
//                )
//            }
//        }
//    }
//}
