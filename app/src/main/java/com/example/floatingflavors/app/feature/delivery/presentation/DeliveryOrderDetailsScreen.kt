package com.example.floatingflavors.app.feature.delivery.presentation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryPermissionHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryOrderDetailsScreen(
    orderId: Int,
    deliveryPartnerId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    val viewModel: DeliveryOrderDetailsViewModel = viewModel(
        factory = DeliveryOrderDetailsViewModelFactory(orderId, deliveryPartnerId)
    )

    val order by viewModel.order.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isTrackingStarted by viewModel.isTrackingStarted.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadOrderDetails()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(rememberScrollState())
    ) {

        // 🔹 Top Bar
        TopAppBar(
            title = { Text("Order Details", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        if (order == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Order not found")
            }
            return@Column
        }

        Column(modifier = Modifier.padding(16.dp)) {

            /* ================= ACCEPT ORDER ================= */

            Button(
                onClick = {

                    // 1️⃣ Location permission check
                    if (!DeliveryPermissionHandler.hasLocationPermission(activity)) {
                        DeliveryPermissionHandler.requestLocationPermission(activity)
                        Toast.makeText(
                            context,
                            "Allow location permission and try again",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    // 2️⃣ GPS enabled check
                    val locationManager =
                        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                    val isGpsEnabled =
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

                    if (!isGpsEnabled) {
                        context.startActivity(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        )
                        Toast.makeText(
                            context,
                            "Turn ON Location and click Accept again",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    // 3️⃣ Accept order (status + delivery_partner_id)
                    viewModel.acceptOrder(context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Accept Order")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.rejectOrder() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reject")
            }

            Spacer(modifier = Modifier.height(16.dp))

            /* ================= GPS SECTION ================= */

            if (
                order?.deliveryPartnerId == deliveryPartnerId.toString() &&
                order?.status.equals("OUT_FOR_DELIVERY", true)
            ) {

                GpsTrackingSection(
                    isTrackingStarted = isTrackingStarted,
                    onStartTracking = {
                        viewModel.startTracking(context)
                    },
                    onStopTracking = {
                        viewModel.stopTracking(context)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.markAsDelivered(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    )
                ) {
                    Text("Mark as Delivered")
                }
            }

            if (order?.deliveryPartnerId != deliveryPartnerId.toString()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(Color(0xFFFFF3E0))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "⚠️ Not Assigned to You",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "This order is assigned to another delivery partner.",
                            textAlign = TextAlign.Center,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
        }
    }
}

/* ================= GPS CARD ================= */

@Composable
fun GpsTrackingSection(
    isTrackingStarted: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(
                "Delivery Tracking",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                if (isTrackingStarted)
                    "✅ GPS tracking is active"
                else
                    "Start GPS tracking when you begin delivery",
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (isTrackingStarted) onStopTracking() else onStartTracking()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (isTrackingStarted) Color(0xFFD32F2F)
                        else Color(0xFF4CAF50)
                )
            ) {
                Text(
                    if (isTrackingStarted) "Stop GPS Tracking"
                    else "Start GPS Tracking",
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun PermissionRequestSection(onRequestPermission: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Location Permission Required",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "You need to grant location permissions to start GPS tracking for deliveries.",
                fontSize = 14.sp,
                color = Color(0xFFE65100).copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Text(
                    text = "Grant Permission",
                    color = Color.White,
                    fontSize = 15.sp
                )
            }
        }
    }
}