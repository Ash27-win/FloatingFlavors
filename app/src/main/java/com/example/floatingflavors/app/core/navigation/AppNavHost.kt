package com.example.floatingflavors.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.floatingflavors.app.feature.auth.presentation.login.LoginScreen
import com.example.floatingflavors.app.feature.auth.presentation.onboarding.OnboardingScreen
import com.example.floatingflavors.app.feature.auth.presentation.splash.SplashScreen
import com.example.floatingflavors.app.feature.user.presentation.home.UserHomeScreen
import com.example.floatingflavors.app.feature.admin.presentation.home.AdminHomeScreen
import com.example.floatingflavors.app.feature.auth.presentation.register.RegisterScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Login (common)
        composable(Screen.Login.route) {
            LoginScreen(
                onBackClick = { navController.popBackStack() },
                onLoginSuccess = { role ->          // 👈 change this name
                    when (role) {
                        "User" -> {
                            navController.navigate(Screen.UserHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                        "Admin" -> {
                            navController.navigate(Screen.AdminHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // 🔹 Register
        composable(Screen.Register.route) {
            RegisterScreen(
                onBackClick = { navController.popBackStack() }, // back arrow
                onRegisterSuccess = {
                    // After register, go back to Login
                    navController.popBackStack(Screen.Login.route, inclusive = false)
                }
            )
        }

        // User Home
        composable(Screen.UserHome.route) {
            UserHomeScreen()
        }

        // Admin Home
        composable(Screen.AdminHome.route) {
            AdminHomeScreen()
        }
    }
}
