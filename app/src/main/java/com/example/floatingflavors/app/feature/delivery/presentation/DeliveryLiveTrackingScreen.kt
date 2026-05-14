package com.example.floatingflavors.app.feature.delivery.presentation

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationStream
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationUpdateService
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryTrackingViewModel
import kotlinx.coroutines.launch

@Composable
fun DeliveryLiveTrackingScreen(
    orderId: Int,
    deliveryPartnerId: Int = 4, // Default to 4 if not passed
    vm: DeliveryTrackingViewModel = viewModel()
) {
    val context = LocalContext.current

    // 1️⃣ Backend Data (Route, ETA, etc)
    val backendDest by vm.destination.collectAsState()
    val route by vm.routePoints.collectAsState()
    val eta by vm.etaMinutes.collectAsState()
    val turn by vm.currentTurn.collectAsState()
    
    // Debug Logging for Backend
    val backendLiveCheck by vm.liveLocation.collectAsState()
    LaunchedEffect(backendLiveCheck) {
        backendLiveCheck?.let {
            android.util.Log.d("FloatingFlavorsGPS", "🌐 BACKEND UPDATE: ${it.latitude}, ${it.longitude}")
        }
    }

    // 2️⃣ Local High-Freq GPS (for Driver Smoothness)
    var localLivePoint by remember { mutableStateOf<org.osmdroid.util.GeoPoint?>(null) }
    var localBearing by remember { mutableStateOf(0f) }
    
    // 3️⃣ Navigation State
    var isNavigating by remember { mutableStateOf(false) }

    /* 🔥 START SERVICE + VM ONLY ONCE */
    LaunchedEffect(orderId) {
        // Start Service (or Update it)
        val intent = Intent(context, DeliveryLocationUpdateService::class.java).apply {
            action = "START_TRACKING"
            putExtra("ORDER_ID", orderId)
            putExtra("DELIVERY_PARTNER_ID", deliveryPartnerId)
        }
        context.startService(intent)
        // Start Data Polling
        vm.startTracking(orderId)
    }
    
    // 🌍 LOCAL GPS (Now via Service Stream for consistency)
    val streamLocation by DeliveryLocationStream.liveLocation.collectAsState()
    val streamBearing by DeliveryLocationStream.bearing.collectAsState() // GPS Bearing
    
    // 🧭 COMPASS (Device Rotation)
    var compassAzimuth by remember { mutableStateOf(0f) }
    DisposableEffect(Unit) {
        val sensor = com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeviceOrientationSensor(context)
        sensor.start()
        val job = kotlinx.coroutines.GlobalScope.launch {
            sensor.azimuth.collect { compassAzimuth = it }
        }
        onDispose { 
            sensor.stop() 
            job.cancel()
        }
    }

    LaunchedEffect(streamLocation) {
        streamLocation?.let {
             localLivePoint = it
             localBearing = streamBearing // GPS Bearing
             android.util.Log.d("FloatingFlavorsGPS", "📡 STREAM GPS: ${it.latitude}, ${it.longitude}")
        }
    }

    // 🔁 Turn instruction UI
    turn?.let {
        TurnCard(
            text = it.text,
            distance = "${it.distanceMeters} m"
        )
    }

    Column(Modifier.fillMaxSize()) {

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
             // Prefer Local GPS, fallback to ViewModel (Backend) if needed
            val backendLive by vm.liveLocation.collectAsState()
            val displayPoint = localLivePoint ?: backendLive
            
            var zoomInTrigger by remember { mutableStateOf(0) }
            var zoomOutTrigger by remember { mutableStateOf(0) }
            
            DeliveryTrackingMap(
                livePoint = displayPoint,
                destination = backendDest,
                routes = if (route.isNotEmpty()) listOf(route) else emptyList(),
                selectedRouteIndex = 0,
                isNavigationStarted = isNavigating, // 🔥 Live Toggle
                bearing = localBearing,             // 🔥 GPS Bearing (Movement)
                compassBearing = compassAzimuth,    // 🔥 Compass Bearing (Stationary)
                zoomInTrigger = zoomInTrigger,
                zoomOutTrigger = zoomOutTrigger,
                modifier = Modifier.fillMaxSize()
            )
            
            // 🧭 ZOOM CONTROLS
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color.White, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { zoomOutTrigger++ }) {
                    Icon(Icons.Default.Remove, "Zoom Out", tint = Color.Black)
                }
                Divider(modifier = Modifier.height(24.dp).width(1.dp), color = Color.LightGray)
                IconButton(onClick = { zoomInTrigger++ }) {
                    Icon(Icons.Default.Add, "Zoom In", tint = Color.Black)
                }
            }
            
            // 🧭 RE-CENTER BUTTON (Small Crosshairs)
            if (!isNavigating) {
                androidx.compose.material3.SmallFloatingActionButton(
                    onClick = { 
                        // Start Navigation / Recenter
                        isNavigating = true
                    },
                    modifier = Modifier
                        .align(androidx.compose.ui.Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 16.dp),
                    containerColor = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                    contentColor = androidx.compose.ui.graphics.Color.Black
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.MyLocation,
                        contentDescription = "Recenter"
                    )
                }
            }
            
            // 🚀 NAVIGATION TOGGLE BUTTON (Main)
            androidx.compose.material3.FloatingActionButton(
                onClick = { isNavigating = !isNavigating },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = if (isNavigating) androidx.compose.ui.graphics.Color.Red else androidx.compose.ui.graphics.Color(0xFF2E63F5)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = if (isNavigating) androidx.compose.material.icons.Icons.Default.Close else androidx.compose.material.icons.Icons.Default.Navigation,
                    contentDescription = "Navigate",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }

        DeliveryBottomSheet(
            etaMin = eta,
            enabled = vm.isArrived(),
            onArrived = { vm.markArrived(orderId) }
        )
    }
}
