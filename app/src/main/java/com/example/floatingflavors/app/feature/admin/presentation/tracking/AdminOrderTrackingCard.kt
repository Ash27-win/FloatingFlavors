package com.example.floatingflavors.app.feature.admin.presentation.tracking

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.floatingflavors.MainActivity
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.admin.presentation.tracking.service.LocationUpdateService
import kotlinx.coroutines.launch

// In AdminOrderTrackingCard.kt - Fix the @Composable

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdminOrderTrackingCard(
    context: Context,
    orderId: Int
) {
    var trackingStarted by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Track permission state
    val activity = context as? MainActivity
    var hasPermission by remember {
        mutableStateOf(activity?.let { AdminPermissionHandler.hasLocationPermission(it) } ?: false)
    }

    // Request permissions on composition
    LaunchedEffect(hasPermission) {
        if (!hasPermission && !trackingStarted && activity != null) {
            AdminPermissionHandler.requestLocationPermission(activity)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Delivery Tracking", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            if (!hasPermission && activity != null) {
                Column {
                    Text(
                        "Location permission required for tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            AdminPermissionHandler.requestLocationPermission(activity)
                            hasPermission = AdminPermissionHandler.hasLocationPermission(activity)
                        }
                    ) {
                        Text("Request Permission")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (trackingStarted)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                ),
                enabled = hasPermission,
                onClick = {
                    if (!trackingStarted) {
                        // Update status to OUT_FOR_DELIVERY first
                        scope.launch {
                            try {
                                // Start GPS service
                                startTracking(context, orderId)
                                trackingStarted = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        stopTracking(context)
                        trackingStarted = false
                    }
                }
            ) {
                Text(if (trackingStarted) "Stop Tracking" else "Start Tracking")
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun startTracking(context: Context, orderId: Int) {
    val intent = Intent(context, LocationUpdateService::class.java).apply {
        putExtra("ORDER_ID", orderId)
        action = "START_TRACKING"
    }
    ContextCompat.startForegroundService(context, intent)
}

private fun stopTracking(context: Context) {
    val intent = Intent(context, LocationUpdateService::class.java).apply {
        action = "STOP_TRACKING"
    }
    context.stopService(intent)
}






//package com.example.floatingflavors.app.feature.admin.presentation.tracking
//
//import android.content.Context
//import android.content.Intent
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.example.floatingflavors.app.core.network.NetworkClient
//import com.example.floatingflavors.app.feature.admin.presentation.tracking.service.LocationUpdateService
//import kotlinx.coroutines.launch
//
//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun AdminOrderTrackingCard(
//    context: Context,
//    orderId: Int
//) {
//    var trackingStarted by remember { mutableStateOf(false) }
//    val scope = rememberCoroutineScope()
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp),
//        elevation = CardDefaults.cardElevation(6.dp)
//    ) {
//        Column(Modifier.padding(16.dp)) {
//
//            Text("Delivery Tracking", style = MaterialTheme.typography.titleMedium)
//            Spacer(Modifier.height(12.dp))
//
//            Button(
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor =
//                        if (trackingStarted)
//                            MaterialTheme.colorScheme.error
//                        else
//                            MaterialTheme.colorScheme.primary
//                ),
//                onClick = {
//                    if (!trackingStarted) {
//                        // 🔥 STEP 1: UPDATE STATUS
//                        scope.launch {
//                            NetworkClient.adminLocationApi.updateLocation(
//                                orderId = orderId,
//                                status = "OUT_FOR_DELIVERY"
//                            )
//                        }
//
//                        // 🔥 STEP 2: START GPS SERVICE
//                        startTracking(context, orderId)
//
//                    } else {
//                        // 🔥 STOP GPS ONLY (STATUS WILL CHANGE ON DELIVERED)
//                        stopTracking(context)
//                    }
//
//                    trackingStarted = !trackingStarted
//                }
//            ) {
//                Text(
//                    if (trackingStarted) "Stop Delivery"
//                    else "Start Delivery"
//                )
//            }
//        }
//    }
//}
//
//@RequiresApi(Build.VERSION_CODES.O)
//private fun startTracking(context: Context, orderId: Int) {
//    val intent = Intent(context, LocationUpdateService::class.java).apply {
//        putExtra("ORDER_ID", orderId)
//    }
//    context.startForegroundService(intent)
//}
//
//private fun stopTracking(context: Context) {
//    val intent = Intent(context, LocationUpdateService::class.java)
//    context.stopService(intent)
//}
