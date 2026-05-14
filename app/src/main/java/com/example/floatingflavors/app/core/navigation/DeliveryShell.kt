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
import com.example.floatingflavors.app.chatbot.ChatRepository
import com.example.floatingflavors.app.chatbot.ChatScreen
import com.example.floatingflavors.app.chatbot.model.ChatViewModel
import com.example.floatingflavors.app.chatbot.data.ChatDatabase
import com.example.floatingflavors.app.feature.delivery.presentation.settings.DeliveryVehicleViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DeliveryShell(
    startRoute: String = Screen.DeliveryDashboard.route,
    rootNavController: NavHostController
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // ✅ CHAT SETUP
    val context = LocalContext.current
    val chatDao = remember { ChatDatabase.getInstance(context).chatDao() }
    val chatViewModel = remember {
        ChatViewModel(
            ChatRepository(
                api = NetworkClient.chatApi,
                dao = chatDao
            )
        )
    }

    // 🔔 NOTIFICATION INTENT HANDLER
    LaunchedEffect(Unit) {
        if (com.example.floatingflavors.app.core.navigation.PendingNotification.hasPending()) {
            val pending = com.example.floatingflavors.app.core.navigation.PendingNotification.consume()
            pending?.let { (screen, refId) ->
                when (screen) {
                    "OrderTrackingScreen", "DeliveryTracking" -> {
                        if (refId.isNotEmpty()) navController.navigate(Screen.DeliveryTracking.createRoute(refId.toInt()))
                    }
                    "DeliveryVehicleInfo" -> navController.navigate(Screen.DeliveryVehicleInfo.route)
                    "DeliveryDocuments" -> navController.navigate(Screen.DeliveryDocuments.route)
                    "DeliveryNotifications" -> navController.navigate(Screen.DeliveryNotifications.route)
                    "DeliveryOrderDetails" -> {
                        if (refId.isNotEmpty()) navController.navigate(Screen.DeliveryOrderDetails.createRoute(refId.toInt()))
                    }
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
                                when (event.screen) {
                                    "OrderTrackingScreen", "DeliveryTracking" -> {
                                        if (!event.referenceId.isNullOrEmpty()) {
                                            navController.navigate(Screen.DeliveryTracking.createRoute(event.referenceId.toInt()))
                                        }
                                    }
                                    "DeliveryVehicleInfo" -> navController.navigate(Screen.DeliveryVehicleInfo.route)
                                    "DeliveryDocuments" -> navController.navigate(Screen.DeliveryDocuments.route)
                                    "DeliveryNotifications" -> navController.navigate(Screen.DeliveryNotifications.route)
                                    "DeliveryOrderDetails" -> {
                                        if (!event.referenceId.isNullOrEmpty()) {
                                            navController.navigate(Screen.DeliveryOrderDetails.createRoute(event.referenceId.toInt()))
                                        }
                                    }
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
                val vm: com.example.floatingflavors.app.feature.delivery.presentation.notifications.DeliveryNotificationViewModel = viewModel()
                com.example.floatingflavors.app.feature.delivery.presentation.notifications.DeliveryNotificationScreen(
                    navController = navController,
                    viewModel = vm
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
                            popUpTo(Screen.DeliveryDashboard.route) { inclusive = false }
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

            /* ---------------- PROFILE ---------------- */

            composable(Screen.DeliveryProfile.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                // Use a shared ViewModel or scoped one if possible, but for now specific per screen
                val vm: com.example.floatingflavors.app.feature.delivery.presentation.profile.DeliveryProfileViewModel = viewModel()

                DeliveryProfileScreen(
                    viewModel = vm,
                    onEditProfile = {
                        navController.navigate(Screen.DeliveryEditProfile.route)
                    },
                    onVehicleInfo = {
                        navController.navigate(Screen.DeliveryVehicleInfo.route)
                    },
                    onDocuments = {
                        navController.navigate(Screen.DeliveryDocuments.route)
                    },
                    onHelpSupport = {
                        navController.navigate(Screen.DeliveryHelpSupport.route)
                    },
                    onLogout = {
                        com.example.floatingflavors.app.core.auth.TokenManager.get(context).clearTokens()
                        com.example.floatingflavors.app.core.UserSession.userId = 0
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.DeliveryRoot.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Screen.DeliveryEditProfile.route) {
                // Ensure we get a fresh VM or same one if scoped? Default is fresh.
                // Re-instantiating here means form state is reset, which is fine.
                val vm: com.example.floatingflavors.app.feature.delivery.presentation.profile.DeliveryProfileViewModel = viewModel()
                
                DeliveryEditProfileScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DeliveryDocuments.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val application = context.applicationContext as android.app.Application
                val vm: com.example.floatingflavors.app.feature.delivery.presentation.settings.DeliveryDocumentsViewModel = viewModel(
                    factory = com.example.floatingflavors.app.feature.delivery.presentation.settings.DeliverySettingsViewModelFactory(
                        application,
                        com.example.floatingflavors.app.core.UserSession.userId,
                        DeliveryRepository(NetworkClient.deliveryApi)
                    )
                )
                com.example.floatingflavors.app.feature.delivery.presentation.settings.DeliveryDocumentsScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DeliveryVehicleInfo.route) {
                val context = androidx.compose.ui.platform.LocalContext.current
                val application = context.applicationContext as android.app.Application
                val vm: com.example.floatingflavors.app.feature.delivery.presentation.settings.DeliveryVehicleViewModel = viewModel(
                    factory = com.example.floatingflavors.app.feature.delivery.presentation.settings.DeliverySettingsViewModelFactory(
                        application,
                        com.example.floatingflavors.app.core.UserSession.userId,
                        DeliveryRepository(NetworkClient.deliveryApi)
                    )
                )
                com.example.floatingflavors.app.feature.delivery.presentation.settings.DeliveryVehicleScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.DeliveryHelpSupport.route) {
                com.example.floatingflavors.app.feature.delivery.presentation.settings.DeliveryHelpSupportScreen(
                    onBack = { navController.popBackStack() },
                    onChatWithSupport = {
                        navController.navigate(Screen.ChatBot.route)
                    }
                )
            }

            composable(
                Screen.ChatBot.route,
                enterTransition = { slideInVertically { it } + fadeIn() },
                exitTransition = { slideOutVertically { it } + fadeOut() }
            ) {
                ChatScreen(
                    userId = com.example.floatingflavors.app.core.UserSession.userId,
                    viewModel = chatViewModel
                )
            }
        }
    }
}
