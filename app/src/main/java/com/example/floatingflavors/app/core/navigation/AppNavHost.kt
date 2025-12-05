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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.floatingflavors.app.feature.auth.presentation.splash.SplashScreen
import com.example.floatingflavors.app.feature.auth.presentation.onboarding.OnboardingScreen
import com.example.floatingflavors.app.feature.auth.presentation.login.LoginScreen
import com.example.floatingflavors.app.feature.auth.presentation.register.RegisterScreen

/**
 * Root NavHost. After Login, it navigates to AdminRoot or UserRoot.
 * AdminRoot and UserRoot are composable containers (shells) with their own inner NavHosts.
 */

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController(), startDestination: String = Screen.Splash.route, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {

        composable(Screen.Splash.route) {
            SplashScreen(onFinished = {
                navController.navigate(Screen.Onboarding.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
            })
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
                        "Admin" -> {
                            // Navigate into admin root (clears login)
                            navController.navigate(Screen.AdminRoot.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "User" -> {
                            navController.navigate(Screen.UserRoot.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        else -> { /* handle other roles if any */ }
                    }
                },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(onBackClick = { navController.popBackStack() }, onRegisterSuccess = {
                navController.popBackStack(Screen.Login.route, inclusive = false)
            })
        }

        // These are the two root containers. They each host their own inner nav graphs.
        composable(Screen.AdminRoot.route) {
            AdminShell() // AdminShell has its own NavController and inner navhost
        }

        composable(Screen.UserRoot.route) {
            UserShell()  // UserShell has its own NavController and inner navhost
        }
    }
}





