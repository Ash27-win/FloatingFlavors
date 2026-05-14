package com.example.floatingflavors.app.feature.delivery.presentation

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryPermissionHandler
import com.example.floatingflavors.app.core.navigation.Screen

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryOrderDetailsScreen(
    orderId: Int,
    deliveryPartnerId: Int,
    onBack: () -> Unit,
    onNavigateToTracking: (Int) -> Unit
) {

    val context = LocalContext.current
    val activity = context as ComponentActivity

    val viewModel: DeliveryOrderDetailsViewModel = viewModel(
        factory = DeliveryOrderDetailsViewModelFactory(orderId, deliveryPartnerId)
    )

    val acceptSuccess by viewModel.acceptSuccess.collectAsState()
    LaunchedEffect(acceptSuccess) {
        if (acceptSuccess) {
            onNavigateToTracking(orderId)
        }
    }

    val order by viewModel.order.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pickupAddress by viewModel.pickupAddress.collectAsState()

    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReasonText by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.loadOrderDetails() }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val data = order ?: return

    // ✅ ON RESUME: Check GPS & Fetch Local Location (User request)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                // If status is NOT yet OUT_FOR_DELIVERY, we want to preview current location
                if (data.status != "OUT_FOR_DELIVERY") {
                    viewModel.fetchDeviceLocation(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(data.status) {
        if (data.status == "OUT_FOR_DELIVERY") {
            viewModel.startObservingPickup(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (DeliveryPermissionHandler.hasLocationPermission(context)) {
                    viewModel.resumeTrackingIfNeeded(context)
                }
            }
        } else {
            // Initial fetch (if permission already granted)
            viewModel.fetchDeviceLocation(context)
        }
    }

    // ✅ FIXED: Allow accepting if Unassigned OR Assigned to me
    val canAccept =
        data.status == "CONFIRMED" &&
                (data.deliveryPartnerId == null || data.deliveryPartnerId == "null" || data.deliveryPartnerId.toIntOrNull() == deliveryPartnerId)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F6))
            .verticalScroll(rememberScrollState())
    ) {

        /* ---------------- TOP BAR ---------------- */
        TopAppBar(
            modifier = Modifier.windowInsetsPadding(WindowInsets(0)),
            title = { Text("Order Details", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null)
                }
            },
            actions = {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.HelpOutline, null)
                }
            }
        )

        /* ---------------- MAP INTEGRATION ---------------- */

        // Map takes priority at top
        val liveAddress = pickupAddress
        // Simple logic: parse Lat/Lng from pickupAddress string if it was just "Loc: x,y"
        // OR use a separate state for actual GeoPoint in ViewModel.


        val routes by viewModel.availableRoutes.collectAsState()
        val selectedIndex by viewModel.selectedRouteIndex.collectAsState()

        Column(Modifier.padding(16.dp)) {

            /* ---------------- ORDER HEADER ---------------- */
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Order #FF-${data.id}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(12.dp))
                Surface(
                    color = Color(0xFFE6F7EC),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        "NEW REQUEST",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color(0xFF16A34A),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row {
                InfoChip(Icons.Default.Payments, "Online Payment")
                Spacer(Modifier.width(8.dp))
                InfoChip(
                    Icons.Default.Schedule,
                    "Est. ${routes.getOrNull(selectedIndex)?.durationMin ?: 25} mins"
                )
            }

            Spacer(Modifier.height(16.dp))

            // 🔥 REJECTION WARNING (NEW)
            if (data.status == "REJECTED") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, Color.Red),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color.Red)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Execution Problem",
                                color = Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Reason: ${data.rejectReason ?: "Unavailable"}",
                            color = Color.Red
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            /* ---------------- PICKUP / DROP CARD ---------------- */
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    val currentDistMeters = routes.getOrNull(selectedIndex)?.distanceMeters
                    val dynamicDistance = if (currentDistMeters != null) String.format("%.1f km away", currentDistMeters / 1000f) else data.distance ?: "..."

                    LocationItem(
                        iconBg = Color(0xFFFFF1E6),
                        icon = Icons.Default.Store,
                        title = "PICKUP",
                        name = "Floating Flavors Restaurant",
                        address = "Mahalakshmi Nagar, Chengalpattu, Tamil Nadu",
                        extra = "" // Fixed location
                    )

                    Spacer(Modifier.height(18.dp))

                    LocationItem(
                        iconBg = Color(0xFFFFEBEE),
                        icon = Icons.Default.LocationOn,
                        title = "DROP-OFF",
                        name = data.customerName ?: "Customer Home",
                        address = data.deliveryAddress ?: "Address unavailable",
                        extra = dynamicDistance
                    )
                }
            }

        } // End Column padding

        Spacer(Modifier.height(16.dp))

        /* ---------------- CUSTOMER CARD ---------------- */
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFE5E5E5), CircleShape)
                )

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(data.customerName, fontWeight = FontWeight.Bold)
                    Text(
                        "Customer since 2021",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                IconButton(
                    modifier = Modifier
                        .size(40.dp)
                        .border(1.dp, Color(0xFFEA580C), CircleShape),
                    onClick = {
                        val phone = data.customerPhone

                        if (!phone.isNullOrBlank()) {
                            context.startActivity(
                                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Customer phone number not available",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.Call, null, tint = Color(0xFFEA580C))
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        /* ---------------- ACTION BUTTONS ---------------- */
        if (canAccept) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 16.dp), // Added padding here
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = { showRejectDialog = true }
                ) {
                    Text("Reject")
                }

                Button(
                    modifier = Modifier.weight(2f),
                    colors = ButtonDefaults.buttonColors(Color(0xFFEA580C)),
                    onClick = {

                        if (!DeliveryPermissionHandler.hasLocationPermission(activity)) {
                            DeliveryPermissionHandler.requestLocationPermission(activity)
                            Toast.makeText(
                                context,
                                "Allow location permission",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        val lm =
                            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            context.startActivity(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            )
                            Toast.makeText(
                                context,
                                "Turn ON GPS to accept order",
                                Toast.LENGTH_LONG
                            ).show()
                            return@Button
                        }

                        viewModel.acceptOrderAndStartTracking(context)
                    }
                ) {
                    Text("Accept Order →")
                }
            }
            if (showRejectDialog) {
                AlertDialog(
                    onDismissRequest = { showRejectDialog = false },
                    title = { Text("Reject Order") },
                    text = {
                        OutlinedTextField(
                            value = rejectReasonText,
                            onValueChange = { rejectReasonText = it },
                            label = { Text("Reason for rejection") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (rejectReasonText.isNotBlank()) {
                                    viewModel.rejectOrder(rejectReasonText)
                                    showRejectDialog = false
                                    rejectReasonText = ""
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please enter a reason",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Confirm Reject", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRejectDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                )
            }
        }
    }
}

/* ---------------- COMPONENTS ---------------- */

    @Composable
    private fun InfoChip(icon: ImageVector, text: String) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF1F1F1)
        ) {
            Row(
                Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, Modifier.size(16.dp), tint = Color.Gray)
                Spacer(Modifier.width(4.dp))
                Text(text, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }

    @Composable
    private fun LocationItem(
        iconBg: Color,
        icon: ImageVector,
        title: String,
        name: String,
        address: String,
        extra: String
    ) {
        Row(verticalAlignment = Alignment.Top) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color(0xFFEA580C))
                }
                Spacer(
                    Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(Color(0xFFE5E7EB))
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 12.sp, color = Color.Gray)
                Text(name, fontWeight = FontWeight.Bold)
                Text(address, fontSize = 13.sp, color = Color.Gray)
                Text(extra, fontSize = 12.sp, color = Color(0xFFEA580C))
            }

            Icon(
                Icons.Default.Navigation,
                null,
                tint = Color(0xFFEA580C),
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFEDD5), CircleShape)
                    .padding(8.dp)
            )
        }
    }
