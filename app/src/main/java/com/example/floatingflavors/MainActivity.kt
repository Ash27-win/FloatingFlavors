package com.example.floatingflavors

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.floatingflavors.app.core.navigation.AppNavHost
import com.example.floatingflavors.app.core.navigation.AppShell
import com.example.floatingflavors.app.core.navigation.Screen
import com.example.floatingflavors.app.core.ui.theme.FloatingFlavorsTheme
import com.example.floatingflavors.app.core.SessionManager
import com.example.floatingflavors.app.core.di.PaymentResultBus


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize UserSession with saved userId
//        UserSession.initialize(this)

//        Log.d("MAIN_ACTIVITY", "Initialized UserSession with userId=${UserSession.userId}")
        setContent {
            FloatingFlavorsApp()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) {
            val response = data?.getStringExtra("response") ?: ""
            if (response.contains("SUCCESS", true)) {
                PaymentResultBus.emit("SUCCESS")
            } else {
                PaymentResultBus.emit("FAILURE")
            }
        }
    }
}

@Composable
fun FloatingFlavorsApp() {
    FloatingFlavorsTheme {
        AppNavHost() // root flow: Splash -> Onboarding -> Login -> AdminRoot/UserRoot
    }
}





//@Composable
//fun FloatingFlavorsApp() {
//    FloatingFlavorsTheme {
//        val navController = rememberNavController()
//        AppNavHost(navController = navController,)
//    }
//}
