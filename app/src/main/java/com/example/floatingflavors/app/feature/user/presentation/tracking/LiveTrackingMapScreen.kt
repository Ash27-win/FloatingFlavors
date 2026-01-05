package com.example.floatingflavors.app.feature.user.presentation.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import org.chromium.base.MathUtils.map
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveTrackingMapScreen(
    navController: NavHostController,
    orderId: Int,
    orderType: String,
    vm: OrderTrackingViewModel = viewModel(),
) {
    val liveLocation by vm.liveLocation.collectAsState()
    val destination by vm.destination.collectAsState()

    LaunchedEffect(Unit) {
        while (true) {
            vm.load(orderId, orderType)
            delay(3000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Tracking") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        SmoothLiveTrackingMap(
            livePoint = liveLocation,
            destination = destination,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        )
    }
}

