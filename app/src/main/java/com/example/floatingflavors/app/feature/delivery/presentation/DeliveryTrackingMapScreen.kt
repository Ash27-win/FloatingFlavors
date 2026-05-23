package com.example.floatingflavors.app.feature.delivery.presentation

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.presentation.tracking.OsmMapComposable
import com.example.floatingflavors.app.feature.user.presentation.tracking.NavigationRoute
import com.example.floatingflavors.app.feature.delivery.domain.VoiceNavigationEngine
import com.example.floatingflavors.app.feature.delivery.domain.TurfGeofenceEngine
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryTrackingViewModel
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint

@Composable
fun DeliveryTrackingMapScreen(
    orderId: Int,
    deliveryPartnerId: Int = 4,
    viewModel: DeliveryTrackingViewModel
) {
    val context = LocalContext.current
    
    // Enterprise Voice Engine
    val voiceEngine = remember { VoiceNavigationEngine(context) }
    DisposableEffect(Unit) {
        onDispose { voiceEngine.shutdown() }
    }
    
    // Connect to ViewModel State
    val currentLat by viewModel.liveLocation.collectAsState()

    /* 🔥 START SERVICE + VM ONLY ONCE */
    LaunchedEffect(orderId) {
        // Start Service (or Update it)
        val intent = android.content.Intent(context, com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationUpdateService::class.java).apply {
            action = "START_TRACKING"
            putExtra("ORDER_ID", orderId)
            putExtra("DELIVERY_PARTNER_ID", deliveryPartnerId)
        }
        context.startService(intent)
        // Start Data Polling
        viewModel.startTracking(orderId)
    }

    // Optimize recompositions using derivedStateOf
    val actualLat by remember { derivedStateOf { currentLat?.latitude ?: 12.9716 } }
    val currentLng by remember { derivedStateOf { currentLat?.longitude ?: 77.5946 } }
    
    // Get bearing from DeliveryLocationStream
    val streamBearing by com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationStream.bearing.collectAsState()
    
    val activeRouteId by viewModel.activeRouteId.collectAsState()
    val routes by viewModel.routes.collectAsState()
    
    val phase by viewModel.navigationPhase.collectAsState()
    val isDelivered by remember { derivedStateOf { phase is com.example.floatingflavors.app.feature.delivery.presentation.tracking.NavigationPhase.ReturningToHub } }
    val isOffline by remember { derivedStateOf { phase is com.example.floatingflavors.app.feature.delivery.presentation.tracking.NavigationPhase.OfflineMode } }
    
    val orderDetails by viewModel.orderDetails.collectAsState()
    var showTripDetails by remember { mutableStateOf(false) }
    
    val rawSpeedKmh by viewModel.speedKmh.collectAsState()
    val rawGpsAccuracy by viewModel.gpsAccuracyMeters.collectAsState()

    // 3️⃣ Navigation State
    var isNavigating by remember { mutableStateOf(false) }

    val speedKmh by remember { derivedStateOf { String.format("%.1f", rawSpeedKmh) } }
    val gpsAccuracy by remember { derivedStateOf { String.format("%.1f", rawGpsAccuracy) } }
    
    val isDeviating by viewModel.isDeviating.collectAsState()
    val distanceKm by viewModel.distanceKm.collectAsState()
    val etaMin by viewModel.etaMinutes.collectAsState()

    // Feature 15: Traffic confidence warning
    val trafficWarning by viewModel.trafficWarningBanner.collectAsState()

    Column(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Map Background
            OsmMapComposable(
            latitude = actualLat,
            longitude = currentLng,
            bearing = streamBearing,
            speedKmh = rawSpeedKmh,
            modifier = Modifier.fillMaxSize(),
            routes = routes,
            activeRouteId = activeRouteId,
            isNavigating = isNavigating,
            onRouteSelected = { routeId ->
                viewModel.selectRoute(routeId)
                voiceEngine.enqueueInstruction("Rerouting to alternate path.", overrideCooldown = true)
            }
        )

        // 🚀 ENTERPRISE NAVIGATION TOP UI STACK
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Offline Banner (Compact)
            if (isOffline) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF111827).copy(alpha = 0.95f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CloudOff, contentDescription = "Offline", tint = Color(0xFFFFC107), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Offline Mode - Syncing when network returns", color = Color(0xFFFFC107), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                    }
                }
            }

            // 2. Traffic Warning
            trafficWarning?.let { banner ->
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFA000).copy(alpha = 0.92f)
                ) {
                    Text(
                        text = banner,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                    )
                }
            }

            // 3. Turn-by-Turn Top Banner
            if (!isDelivered && isNavigating) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E88E5))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.TurnRight, contentDescription = "Turn", tint = Color.White, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("In 200 meters", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                            Text("Turn right onto Main Street", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                        }
                    }
                }
            }

            // 4. Telemetry Row (Speed / GPS)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Speed
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = "Speed", modifier = Modifier.size(20.dp), tint = Color(0xFF1E293B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "$speedKmh km/h", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                            Text(text = "Speed", fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                        }
                    }
                }
                
                // GPS
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier.weight(1f).padding(start = 6.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GpsFixed, contentDescription = "GPS", modifier = Modifier.size(20.dp), tint = if (rawGpsAccuracy < 10) Color(0xFF10B981) else Color(0xFFEF4444))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "±${gpsAccuracy}m", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                            Text(text = "GPS Accuracy", fontSize = 10.sp, color = Color(0xFF64748B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                        }
                    }
                }
            }

            // 5. Horizontal Multiple-Route Selector
            if (routes.size > 1 && !isNavigating) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(routes) { index, route ->
                        val isActive = route.id == activeRouteId
                        val label = if (index == 0) "Fastest" else if (index == 1) "Less Traffic" else "Shortest"
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isActive) Color(0xFF2E63F5) else Color.White,
                            contentColor = if (isActive) Color.White else Color(0xFF1E293B),
                            shadowElevation = if (isActive) 8.dp else 2.dp,
                            modifier = Modifier.clickable { 
                                viewModel.selectRoute(route.id)
                                voiceEngine.enqueueInstruction("Rerouting to $label path.", overrideCooldown = true)
                            }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.Medium, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                                Text(text = "${route.totalEtaMinutes} min", fontSize = 14.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                            }
                        }
                    }
                }
            }
            
            // 6. Delivery Completed Banner
            if (isDelivered) {
                Card(
                    modifier = Modifier.padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Delivery Completed! 🎉", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Return to Hub: 2/116, Kanni kovil street", fontSize = 12.sp, color = Color.Gray, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                    }
                }
            }
        }

        // 🎛️ Right-Side Action Buttons (Vertical Stack)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // SOS Emergency Button (Top of stack)
            Surface(
                onClick = { voiceEngine.speakUrgent("Emergency SOS activated.") },
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEA4335), // Red
                shadowElevation = 8.dp,
                modifier = Modifier.size(48.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Security, contentDescription = "SOS", tint = Color.White, modifier = Modifier.size(20.dp))
                    Text("SOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                }
            }
            
            // Scope / Recenter Button
            FloatingActionButton(
                onClick = { /* Recenter */ },
                containerColor = Color.White,
                contentColor = Color(0xFF1E293B),
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter", modifier = Modifier.size(24.dp))
            }
            
            // Zoom In (+)
            FloatingActionButton(
                onClick = { /* Visual only for now */ },
                containerColor = Color.White,
                contentColor = Color(0xFF1E293B),
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Medium, fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif)
            }

            // Zoom Out (-)
            FloatingActionButton(
                onClick = { /* Visual only for now */ },
                containerColor = Color.White,
                contentColor = Color(0xFF1E293B),
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Text("-", fontSize = 24.sp, fontWeight = FontWeight.Medium, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
            }
        }

        // 🚀 NAVIGATION TOGGLE BUTTON (Main Bottom Center)
        androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = { 
                isNavigating = !isNavigating
                if (isNavigating) {
                    voiceEngine.speakUrgent("Starting navigation. Please proceed to the route.")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp), // Hover cleanly above bottom sheet
            containerColor = if (isNavigating) Color.Red else Color(0xFF1D4ED8), // Deep premium blue
            contentColor = Color.White,
            shape = RoundedCornerShape(32.dp),
            icon = {
                Icon(
                    imageVector = if (isNavigating) androidx.compose.material.icons.Icons.Default.Close else androidx.compose.material.icons.Icons.Default.Navigation,
                    contentDescription = "Navigate"
                )
            },
            text = {
                Text(if (isNavigating) "Exit" else "Start Navigation", fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
            }
        )

        // Polygon Hub Geofence auto-arrival
        LaunchedEffect(currentLat) {
            currentLat?.let { point ->
                if (phase is com.example.floatingflavors.app.feature.delivery.presentation.tracking.NavigationPhase.ReturningToHub) {
                    if (TurfGeofenceEngine.isInsideHubGeofence(GeoPoint(point.latitude, point.longitude))) {
                        voiceEngine.speakUrgent("You have safely returned to the Floating Flavors Cloud Kitchen.")
                    }
                }
            }
        }
    } // End of Map Box

    // Format distance cleanly
    val formattedDistance = remember(distanceKm) { String.format("%.1f", distanceKm) }

    // Bottom Sheet UI
    val customerPhone = "9876543210" // Not available in DeliveryOrderData, fallback
    DeliveryBottomSheet(
        orderId = orderDetails?.data?.id ?: "FF-902",
        customerName = orderDetails?.data?.customerName ?: "Customer",
        customerPhone = customerPhone,
        paymentMethod = "Cash",
        paymentAmount = orderDetails?.data?.amount ?: "587",
        etaMin = etaMin,
        distanceStr = formattedDistance,
        address = orderDetails?.data?.deliveryAddress ?: "Kalpakkam, Chennai",
        enabled = viewModel.isArrived(),
        onArrived = { viewModel.markArrived(orderId) }
    )

    // Trip Details Modal
    if (showTripDetails) {
        TripDetailsModal(
            orderDetails = orderDetails,
            onDismiss = { showTripDetails = false }
        )
    }
} // End of Column
} // End of DeliveryTrackingMapScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailsModal(
    orderDetails: com.example.floatingflavors.app.feature.delivery.data.DeliveryOrderDetailsResponse?,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header (Auto/Bike image and name)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DirectionsBike,
                    contentDescription = "Bike",
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFFFBC02D)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text("Bike Delivery", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // Order details summary
            if (orderDetails?.data != null) {
                Text("Order ID: #${orderDetails.data.id ?: "N/A"}", fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                val itemsList = orderDetails.data.items ?: emptyList()
                Text("Food Items (${itemsList.size}):", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                itemsList.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.qty ?: 1}x ${item.name ?: "Unknown Item"}")
                        Text("") // Removed individual price as it is not in OrderItemDto
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Fare", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("₹${orderDetails.data.amount ?: "0"}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = "Cash", modifier = Modifier.size(20.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paying via Cash", color = Color.DarkGray)
                }
            } else {
                Text("Fetching Order Details...", color = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Back to Tracking")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun DeliveryBottomSheet(
    orderId: String,
    customerName: String,
    customerPhone: String,
    paymentMethod: String,
    paymentAmount: String,
    etaMin: Int,
    distanceStr: String,
    address: String,
    enabled: Boolean,
    onArrived: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 24.dp
    ) {
        Column(Modifier.padding(top = 12.dp, start = 20.dp, end = 20.dp, bottom = 24.dp)) {
            // Drag Handle
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                )
            }
            
            // ETA & Distance Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$etaMin min", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(4.dp).background(Color(0xFF94A3B8), androidx.compose.foundation.shape.CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("$distanceStr km", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color(0xFF64748B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                }
                
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = "Expand",
                    tint = Color(0xFF475569),
                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    // Payment Banner
                    val isPaid = paymentMethod.equals("ONLINE", ignoreCase = true)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = if (isPaid) Color(0xFFE8F5E9) else Color(0xFFFFF3E0) // Light Green or Orange
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(if (isPaid) Color(0xFF4CAF50) else Color(0xFFFF9800), androidx.compose.foundation.shape.CircleShape))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isPaid) "PAID ONLINE" else "CASH TO COLLECT ₹$paymentAmount",
                                fontWeight = FontWeight.Bold,
                                color = if (isPaid) Color(0xFF2E7D32) else Color(0xFFE65100),
                                fontSize = 14.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Default
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Customer & Contact Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(customerName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                            Text("Order #$orderId", fontSize = 12.sp, color = Color(0xFF64748B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                        }
                        
                        Row {
                            // Call Button
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color(0xFFE8F5E9),
                                modifier = Modifier.size(44.dp).clickable {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                                    intent.data = android.net.Uri.parse("tel:$customerPhone")
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = Color(0xFF10B981), modifier = Modifier.padding(10.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            // Chat Button
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color(0xFFE3F2FD),
                                modifier = Modifier.size(44.dp).clickable {
                                    val url = "https://wa.me/91$customerPhone"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                                    intent.data = android.net.Uri.parse(url)
                                    context.startActivity(intent)
                                }
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color(0xFF3B82F6), modifier = Modifier.padding(10.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Location Info
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = Color(0xFFE11D48), modifier = Modifier.size(20.dp).padding(top = 2.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Delivery Address", fontSize = 12.sp, color = Color(0xFF64748B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(address, fontSize = 14.sp, color = Color(0xFF1E293B), fontFamily = androidx.compose.ui.text.font.FontFamily.Default, lineHeight = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Button (Slide to Deliver / Arrived)
                    Button(
                        onClick = onArrived,
                        enabled = enabled,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("SLIDE TO DELIVER", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Default)
                    }
                }
            }
        }
    }
}
