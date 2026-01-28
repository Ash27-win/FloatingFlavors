//package com.example.floatingflavors.app.core.navigation
//
//import androidx.compose.runtime.Composable
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import com.example.floatingflavors.app.feature.auth.presentation.login.LoginScreen
//import com.example.floatingflavors.app.feature.auth.presentation.onboarding.OnboardingScreen
//import com.example.floatingflavors.app.feature.auth.presentation.splash.SplashScreen
//import com.example.floatingflavors.app.feature.user.presentation.home.UserHomeScreen
//import com.example.floatingflavors.app.feature.admin.presentation.home.AdminHomeScreen
//import com.example.floatingflavors.app.feature.admin.presentation.menu.AdminAddFoodScreen
//import com.example.floatingflavors.app.feature.auth.presentation.register.RegisterScreen
//import com.example.floatingflavors.app.feature.user.presentation.menu.UserMenuScreen
//
//@Composable
//fun AppNavHost(navController: NavHostController) {
//    NavHost(
//        navController = navController,
//        startDestination = Screen.Splash.route
//    ) {
//        // Splash
//        composable(Screen.Splash.route) {
//            SplashScreen(
//                onFinished = {
//                    navController.navigate(Screen.Onboarding.route) {
//                        popUpTo(Screen.Splash.route) { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // Onboarding
//        composable(Screen.Onboarding.route) {
//            OnboardingScreen(
//                onFinished = {
//                    navController.navigate(Screen.Login.route) {
//                        popUpTo(Screen.Onboarding.route) { inclusive = true }
//                    }
//                }
//            )
//        }
//
//        // Login (common)
//        composable(Screen.Login.route) {
//            LoginScreen(
//                onBackClick = { navController.popBackStack() },
//                onLoginClick = { role ->          // 👈 change this name
//                    when (role) {
//                        "User" -> {
//                            navController.navigate(Screen.UserHome.route) {
//                                popUpTo(Screen.Login.route) { inclusive = true }
//                            }
//                        }
//                        "Admin" -> {
//                            navController.navigate(Screen.AdminHome.route) {
//                                popUpTo(Screen.Login.route) { inclusive = true }
//                            }
//                        }
//                    }
//                },
//                onNavigateToRegister = {
//                    navController.navigate(Screen.Register.route)
//                }
//            )
//        }
//
//        // 🔹 Register
//        composable(Screen.Register.route) {
//            RegisterScreen(
//                onBackClick = { navController.popBackStack() }, // back arrow
//                onRegisterSuccess = {
//                    // After register, go back to Login
//                    navController.popBackStack(Screen.Login.route, inclusive = false)
//                }
//            )
//        }
//
//        // User Home
//        composable(Screen.UserHome.route) {
//            UserHomeScreen()
//        }
//
//        // Admin Home (pass navController)
//        composable(Screen.AdminHome.route) {
//            AdminHomeScreen(navController = navController, onLogout = {
//                // optional: navigate back to login
//                navController.popBackStack(Screen.Login.route, inclusive = false)
//                navController.navigate(Screen.Login.route)
//            })
//        }
//
//// Admin Add Food (ensure this composable exists)
//        composable(Screen.AdminAddFood.route) {
//            AdminAddFoodScreen(
//                onBackClick = { navController.popBackStack() },
//                onAdded = {
//                    // Optionally show a snackbar or navigate back:
//                    navController.popBackStack(Screen.AdminAddFood.route, inclusive = true)
//                    // or simply navController.popBackStack()
//                }
//            )
//        }
//
//        composable(Screen.UserMenu.route) {
//            UserMenuScreen()
//        }
//    }
//}

package com.example.floatingflavors.app.core.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.floatingflavors.app.feature.auth.data.AuthRepository
import com.example.floatingflavors.app.feature.auth.presentation.forgot.AccountRecoveryScreen
import com.example.floatingflavors.app.feature.auth.presentation.forgot.ForgotPasswordViewModel
import com.example.floatingflavors.app.feature.auth.presentation.forgot.ResetPasswordScreen
import com.example.floatingflavors.app.feature.auth.presentation.forgot.ResetPasswordViewModel
import com.example.floatingflavors.app.feature.auth.presentation.forgot.VerificationScreen
import com.example.floatingflavors.app.feature.auth.presentation.forgot.VerifyOtpViewModel
import com.example.floatingflavors.app.feature.auth.presentation.splash.SplashScreen
import com.example.floatingflavors.app.feature.auth.presentation.onboarding.OnboardingScreen
import com.example.floatingflavors.app.feature.auth.presentation.login.LoginScreen
import com.example.floatingflavors.app.feature.auth.presentation.register.RegisterScreen

/**
 * Root NavHost. After Login, it navigates to AdminRoot or UserRoot.
 * AdminRoot and UserRoot are composable containers (shells) with their own inner NavHosts.
 */

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController(), startDestination: String = Screen.Splash.route, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToDashboard = { role ->
                    // Auto-Login Success
                    when (role) {
                        "Admin" -> navController.navigate(Screen.AdminRoot.route) {
                             popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                        "User" -> navController.navigate(Screen.UserRoot.route) {
                             popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                        "Delivery" -> navController.navigate(Screen.DeliveryRoot.route) {
                             popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                },
                onNavigateToLogin = {
                    // No Token / Expired -> Go to Onboarding
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.Login.route) { popUpTo(Screen.Onboarding.route) { inclusive = true } }
            })
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginClick = { role ->
                    when (role) {
                        "Admin" -> navController.navigate(Screen.AdminRoot.route)
                        "User" -> navController.navigate(Screen.UserRoot.route)
                        "Delivery" -> navController.navigate(Screen.DeliveryRoot.route)
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onForgotPassword = {                      // ✅ THIS WAS MISSING
                    navController.navigate(Screen.ForgotPassword.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(onBackClick = { navController.popBackStack() }, onRegisterSuccess = {
                navController.popBackStack(Screen.Login.route, inclusive = false)
            })
        }

        // These are the two root containers. They each host their own inner nav graphs.
        composable(Screen.AdminRoot.route) {
            AdminShell(rootNavController = navController)
        }

        composable(Screen.UserRoot.route) {
            UserShell(rootNavController = navController)
        }

        composable(Screen.DeliveryRoot.route) {
            DeliveryShell(rootNavController = navController)
        }

        composable(Screen.ForgotPassword.route) {

            var email by rememberSaveable { mutableStateOf("") }

            val repository = remember { AuthRepository() }
            val vm = remember { ForgotPasswordViewModel(repository) }


            val state by vm.state.collectAsState()

            AccountRecoveryScreen(
                loading = state.loading,
                message = state.message,
                onBack = { navController.popBackStack() },
                onSendOtp = {
                    email = it
                    vm.sendOtp(it)
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route)
                }
            )

            LaunchedEffect(state.success) {
                if (state.success) {
                    navController.navigate(
                        Screen.VerifyOtp.createRoute(email)
                    )
                }
            }
        }

        composable(
            route = Screen.VerifyOtp.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { entry ->

            val email = entry.arguments!!.getString("email")!!
            val vm = remember {
                VerifyOtpViewModel(AuthRepository())
            }

            val state by vm.state.collectAsState()

            VerificationScreen(
                emailMasked = email.replaceAfter("@", "***"),
                seconds = state.seconds,
                loading = state.loading,
                message = state.message,
                onBack = { navController.popBackStack() },
                onVerify = { otp ->
                    vm.verifyOtp(email, otp)
                },
                onResend = {
                    vm.resendOtp(email)
                }
            )

            LaunchedEffect(state.success) {
                if (state.success) {
                    navController.navigate(
                        Screen.ResetPassword.createRoute(email)
                    )
                }
            }
        }

        composable(
            route = Screen.ResetPassword.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { entry ->

            val email = entry.arguments!!.getString("email")!!
            val vm = remember {
                ResetPasswordViewModel(AuthRepository())
            }

            val state by vm.state.collectAsState()

            ResetPasswordScreen(
                loading = state.loading,
                message = state.message,
                onBack = { navController.popBackStack() },
                onPasswordUpdated = { password ->
                    vm.resetPassword(email, password)
                },
                onLoginClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )

            LaunchedEffect(state.success) {
                if (state.success) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
        }

    }
}










