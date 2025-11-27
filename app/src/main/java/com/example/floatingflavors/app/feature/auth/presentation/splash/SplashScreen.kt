package com.example.floatingflavors.app.feature.auth.presentation.splash

// feature/auth/presentation/splash/SplashScreen.kt


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.floatingflavors.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(3000)   // 3 seconds
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),   // or light grey like your design
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_floating_flavors),
            contentDescription = "Cloud Kitchen logo",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .background(Color.White),
        )
    }
}
