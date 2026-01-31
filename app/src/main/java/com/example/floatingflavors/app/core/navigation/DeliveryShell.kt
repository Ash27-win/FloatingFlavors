package com.example.floatingflavors.app.core.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.DeliveryTrackingRepository
import com.example.floatingflavors.app.feature.delivery.presentation.*
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryTrackingViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeliveryShell(
    startRoute: String = Screen.DeliveryDashboard.route,
    rootNavController: NavHostController
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // 🔔 NOTIFICATION INTENT HANDLER
    LaunchedEffect(Unit) {
        if (com.example.floatingflavors.app.core.navigation.PendingNotification.hasPending()) {
            val pending = com.example.floatingflavors.app.core.navigation.PendingNotification.consume()
            pending?.let { (screen, refId) ->
                if (screen == "OrderTrackingScreen" && refId.isNotEmpty()) {
                    navController.navigate(
                        Screen.DeliveryTracking.createRoute(refId.toInt())
                    )
                }
            }
        }
    }

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

            /* ---------------- DASHBOARD ---------------- */

            composable(Screen.DeliveryDashboard.route) {

                val deliveryPartnerId = com.example.floatingflavors.app.core.UserSession.userId

                val repository = DeliveryRepository(NetworkClient.deliveryApi)
                
                val context = androidx.compose.ui.platform.LocalContext.current
                val notificationRepository = remember {
                     com.example.floatingflavors.app.feature.notification.data.NotificationRepository(
                        NetworkClient.notificationApi,
                        com.example.floatingflavors.app.core.data.local.AppDatabase.getDatabase(context)
                    )
                }

                val factory = DeliveryDashboardViewModelFactory(
                    repository = repository,
                    notificationRepository = notificationRepository,
                    deliveryPartnerId = deliveryPartnerId
                )

                val dashboardViewModel: DeliveryDashboardViewModel =
                    viewModel(factory = factory)

                // 🔥 LISTEN FOR REAL-TIME NOTIFICATIONS
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                LaunchedEffect(lifecycleOwner) {
                    com.example.floatingflavors.app.core.service.NotificationEventBus.events.collect { event: com.example.floatingflavors.app.core.service.NotificationEvent ->
                        when (event) {
                            is com.example.floatingflavors.app.core.service.NotificationEvent.NewOrder -> {
                                dashboardViewModel.refreshOrders()
                            }
                            is com.example.floatingflavors.app.core.service.NotificationEvent.Navigate -> {
                                if (event.screen == "OrderTrackingScreen" && !event.referenceId.isNullOrEmpty()) {
                                    navController.navigate(
                                        Screen.DeliveryTracking.createRoute(event.referenceId.toInt())
                                    )
                                }
                            }
                        }
                    }
                }

                DeliveryDashboardScreen(
                    viewModel = dashboardViewModel,
                    onViewDetails = { orderId ->
                        val activeOrder = dashboardViewModel.state.value
                            ?.activeOrder
                            ?.takeIf { it.id == orderId }

                        if (activeOrder?.status == "OUT_FOR_DELIVERY") {
                            navController.navigate(
                                Screen.DeliveryTracking.createRoute(orderId)
                            )
                        } else {
                            navController.navigate(
                                Screen.DeliveryOrderDetails.createRoute(orderId)
                            )
                        }
                    },
                    onLiveTracking = { orderId ->
                        navController.navigate(
                             Screen.DeliveryTracking.createRoute(orderId.toInt())
                        )
                    },
                    onNotificationClick = {
                        navController.navigate(Screen.DeliveryNotifications.route)
                    }
                )
            }
            
            /* ---------------- NOTIFICATIONS ---------------- */
            composable(Screen.DeliveryNotifications.route) {
                // Ensure Application context is available
                val vm: com.example.floatingflavors.app.feature.delivery.presentation.notification.DeliveryNotificationViewModel = viewModel()
                com.example.floatingflavors.app.feature.delivery.presentation.notification.DeliveryNotificationScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            /* ---------------- ORDER DETAILS ---------------- */

            composable(
                route = Screen.DeliveryOrderDetails.route,
                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
            ) { backStackEntry ->

                val orderId = backStackEntry.arguments!!.getInt("orderId")
                val deliveryPartnerId = 4 // TODO: from session

                DeliveryOrderDetailsScreen(
                    orderId = orderId,
                    deliveryPartnerId = deliveryPartnerId,
                    onBack = { navController.navigateUp() },
                    onNavigateToTracking = { id ->
                        navController.navigate(
                            Screen.DeliveryTracking.createRoute(id)
                        ) {
                            popUpTo(Screen.DeliveryOrderDetails.route) { inclusive = true }
                        }
                    }
                )
            }

            /* ---------------- TRACKING MAP ---------------- */

            /* ---------------- TRACKING MAP ---------------- */

            composable(
                route = Screen.DeliveryTracking.route,
                arguments = listOf(navArgument("orderId") { type = NavType.IntType })
            ) { backStackEntry ->

                val orderId = backStackEntry.arguments!!.getInt("orderId")
                val deliveryPartnerId = com.example.floatingflavors.app.core.UserSession.userId

                // ✅ ViewModel WITHOUT factory (factory was removed)
                val trackingVm: DeliveryTrackingViewModel = viewModel()

                DeliveryLiveTrackingScreen(
                    orderId = orderId,
                    deliveryPartnerId = deliveryPartnerId, // 🔥 Pass ID for DB updates
                    vm = trackingVm
                )
            }


            /* ---------------- PROFILE ---------------- */

            composable(Screen.DeliveryProfile.route) {
                // Get context for logout
                val context = androidx.compose.ui.platform.LocalContext.current

                DeliveryProfileScreen(
                    onLogout = {
                        // 🔥 Clear Session
                        com.example.floatingflavors.app.core.auth.TokenManager.get(context).clearTokens()
                        com.example.floatingflavors.app.core.UserSession.userId = 0

                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.DeliveryRoot.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
