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
import com.example.floatingflavors.app.feature.user.data.booking_checkout.AddressCheckoutRepository
import com.example.floatingflavors.app.feature.user.data.settings.*
import com.example.floatingflavors.app.feature.user.presentation.UserHomeScreen
import com.example.floatingflavors.app.feature.user.presentation.booking.BookingScreen
import com.example.floatingflavors.app.feature.user.presentation.booking.BookingViewModel
import com.example.floatingflavors.app.feature.user.presentation.booking.EventMenuScreen
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.CheckoutAddressScreen
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.CheckoutAddressViewModel
import com.example.floatingflavors.app.feature.user.presentation.membership.MembershipScreen
import com.example.floatingflavors.app.feature.user.presentation.menu.UserMenuGridScreen
import com.example.floatingflavors.app.feature.user.presentation.settings.*
import com.example.floatingflavors.app.feature.user.presentation.settings.edit.*
import com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress.*
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import com.example.floatingflavors.app.feature.user.data.booking_checkout.CheckoutSummaryRepository
import com.example.floatingflavors.app.feature.user.data.booking_checkout.PaymentRepository
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.CheckoutSummaryViewModel
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.CheckoutPaymentScreen
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.CheckoutPaymentViewModel
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.CheckoutSummaryScreen
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.OrderSuccessScreen
import com.example.floatingflavors.app.feature.orders.data.OrdersRepository
import com.example.floatingflavors.app.feature.user.data.order.UserOrdersRepository
import com.example.floatingflavors.app.feature.user.presentation.order.OrderDetailsScreen
import com.example.floatingflavors.app.feature.user.presentation.order.OrderDetailsViewModel
import com.example.floatingflavors.app.feature.user.presentation.order.UserOrdersScreen
import com.example.floatingflavors.app.feature.user.presentation.order.UserOrdersViewModel
import com.example.floatingflavors.app.feature.user.presentation.tracking.LiveOrderTrackingScreen
import com.example.floatingflavors.app.feature.user.presentation.notification.UserNotificationScreen
import com.example.floatingflavors.app.feature.user.presentation.tracking.LiveTrackingMapScreen


@Composable
fun UserShell(
    startRoute: String = Screen.UserHome.route
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val hideBottomBar =
        currentRoute?.startsWith("user_booking_menu") == true ||
                currentRoute == Screen.UserNotifications.route


    // 🔥 SINGLE SOURCE OF TRUTH
    val addressViewModel = remember {
        AddressViewModel(AddressRepository(NetworkClient.addressApi))
    }

    val editAddressViewModel = remember {
        EditAddressViewModel(AddressRepository(NetworkClient.addressApi))
    }

    // CHECKOUT SCREEN ADDRESS
    val checkoutAddressVm = remember {
        CheckoutAddressViewModel(
            AddressCheckoutRepository(NetworkClient.addressCheckoutApi)
        )
    }

    val checkoutSummaryVm = remember {
        CheckoutSummaryViewModel(
            CheckoutSummaryRepository(NetworkClient.checkoutSummaryApi)
        )
    }

    val checkoutPaymentVm = remember {
        CheckoutPaymentViewModel(
            PaymentRepository(NetworkClient.paymentApi)
        )
    }




    Scaffold(
        bottomBar = {
            if (!hideBottomBar) {
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
                    },
                    onOpenNotifications = {
                        navController.navigate(Screen.UserNotifications.route)
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
                        // bookingId is already Int from BookingViewModel's navigation
                        navController.navigate(
                            Screen.UserBookingMenu.createRoute(bookingId)
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
                    onBack = {
                        navController.navigate(Screen.UserHome.route) {
                            popUpTo(Screen.UserHome.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToCheckout = { bookingId ->
                        navController.navigate(
                            Screen.CheckoutAddress.createRoute(bookingId)
                        )
                    }
                )
            }

            // CHECKOUT SCREEN ADDRESS
            composable(
                route = Screen.CheckoutAddress.route,
                arguments = listOf(navArgument("bookingId") { type = NavType.IntType }),
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } }
            ) { entry ->
                val bookingId = entry.arguments!!.getInt("bookingId")

                CheckoutAddressScreen(
                    vm = checkoutAddressVm,
                    userId = 1,
                    onBack = { navController.popBackStack() },
                    onAddAddress = {
                        navController.navigate(Screen.AddAddress.route)
                    },
                    onContinue = { addressId ->
                        navController.navigate(
                            Screen.CheckoutSummary.createRoute(bookingId, addressId)
                        )
                    }
                )
            }

            composable(
                route = Screen.CheckoutSummary.route,
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.IntType },
                    navArgument("addressId") { type = NavType.IntType }
                ),
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { entry ->
                val bookingId = entry.arguments!!.getInt("bookingId")
                val addressId = entry.arguments!!.getInt("addressId")

                CheckoutSummaryScreen(
                    vm = checkoutSummaryVm,
                    userId = 1,
                    bookingId = bookingId,
                    addressId = addressId,
                    onBack = { navController.popBackStack() },
                    onChangeAddress = {
                        navController.popBackStack()
                    },
                    onContinue = {
                        navController.navigate(
                            Screen.CheckoutPayment.createRoute(bookingId, addressId)
                        )
                    }
                )
            }

            composable(
                route = Screen.CheckoutPayment.route,
                arguments = listOf(
                    navArgument("bookingId") { type = NavType.IntType },
                    navArgument("addressId") { type = NavType.IntType }
                ),
                enterTransition = { slideInHorizontally { it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { entry ->

                val bookingId = entry.arguments!!.getInt("bookingId")

                CheckoutPaymentScreen(
                    bookingId = bookingId,
                    totalAmount = checkoutSummaryVm.uiState.total,
                    vm = checkoutPaymentVm,
                    onBack = { navController.popBackStack() },
                    onPaymentSuccess = { txnId, method ->
                        navController.navigate(
                            Screen.OrderSuccess.createRoute(
                                txnId = txnId,
                                method = method
                            )
                        )
                    }
                )
            }

            composable(
                route = Screen.OrderSuccess.route,
                arguments = listOf(
                    navArgument("txnId") { type = NavType.StringType },
                    navArgument("method") { type = NavType.StringType }
                ),
                enterTransition = {
                    fadeIn() + scaleIn(initialScale = 0.9f)
                }
            ) { entry ->

                val txnId = entry.arguments!!.getString("txnId")!!
                val method = entry.arguments!!.getString("method")!!

                OrderSuccessScreen(
                    transactionId = txnId,
                    paymentMethod = method,
                    onBack = { navController.popBackStack() },
                    onBackToHome = {
                        navController.navigate(Screen.UserHome.route) {
                            popUpTo(Screen.UserHome.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.UserOrders.route) {

                val vm = remember {
                    UserOrdersViewModel(
                        ordersRepository = OrdersRepository(),
                        bookingRepository = BookingRepository(NetworkClient.bookingApi),
                        userId = 1
                    )
                }

                UserOrdersScreen(
                    viewModel = vm,
                    onOpenOrderDetails = { orderId ->
                        navController.navigate(
                            Screen.UserOrderDetails.createRoute(orderId)
                        )
                    }
                )
            }

            composable(
                route = Screen.UserOrderDetails.route,
                arguments = listOf(
                    navArgument("orderId") { type = NavType.StringType }
                ),
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } }
            ) { entry ->

                val orderId = entry.arguments!!.getString("orderId")!!

                val orderDetailsVm = remember {
                    OrderDetailsViewModel(
                        ordersRepo = UserOrdersRepository(NetworkClient.userOrdersApi),
                        addressRepo = AddressRepository(NetworkClient.addressApi),
                        userId = 1
                    )
                }

                OrderDetailsScreen(
                    orderId = orderId,
                    viewModel = orderDetailsVm,
                    onBack = { navController.popBackStack() },
                    onTrack = {
                        navController.navigate(
                            Screen.UserOrderTracking.createRoute(
                                orderId = orderId.toInt(),
                                type = "INDIVIDUAL" // or EVENT / COMPANY
                            )
                        )
                    }
                )
            }

            composable(
                route = "user_order_tracking/{orderId}/{orderType}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.IntType },
                    navArgument("orderType") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val orderId = backStackEntry.arguments!!.getInt("orderId")
                val orderType = backStackEntry.arguments!!.getString("orderType")!!

                LiveOrderTrackingScreen(
                    navController = navController,
                    orderId = orderId,
                    orderType = orderType
                )
            }

            composable(
                route = "live_map/{orderId}/{orderType}",
                arguments = listOf(
                    navArgument("orderId") { type = NavType.IntType },
                    navArgument("orderType") { type = NavType.StringType }
                )
            ) { backStackEntry ->

                val orderId = backStackEntry.arguments!!.getInt("orderId")
                val orderType = backStackEntry.arguments!!.getString("orderType")!!

                LiveTrackingMapScreen(
                    navController = navController,
                    orderId = orderId,
                    orderType = orderType
                )
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

            composable(Screen.UserNotifications.route) {
                UserNotificationScreen(
                    onClose = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}