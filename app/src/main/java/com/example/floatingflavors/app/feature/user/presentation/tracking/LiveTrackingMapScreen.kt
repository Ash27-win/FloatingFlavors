package com.example.floatingflavors.app.feature.user.presentation.tracking

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.shadow

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
    val state by vm.state.collectAsState()
    val routes by vm.routes.collectAsState()

    LaunchedEffect(orderId) {
        vm.load(orderId, orderType)
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
        if (liveLocation != null && destination != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Background Map
                OsmMapComposable(
                    latitude = liveLocation!!.latitude,
                    longitude = liveLocation!!.longitude,
                    modifier = Modifier.fillMaxSize(),
                    routes = routes,
                    isNavigating = false // False so it draws blue route
                )
                
                // Rapido-Style Bottom Sheet (Overlay)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .background(Color.White)
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Handle bar
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .background(Color.LightGray, RoundedCornerShape(2.dp))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(Modifier.height(16.dp))

                        val eta = routes.firstOrNull()?.totalEtaMinutes ?: 15
                        val dist = routes.firstOrNull()?.totalDistanceKm ?: 2.5

                        Text(
                            "Rider arriving in $eta mins",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            String.format("Destination is %.1f km away", dist),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        
                        Spacer(Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                        Spacer(Modifier.height(20.dp))

                        // Rider Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color(0xFFE0E0E0), androidx.compose.foundation.shape.CircleShape)
                                    .clip(androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    state?.deliveryPerson?.name ?: "Delivery Partner",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Text(
                                    state?.deliveryPerson?.vehicle ?: "Bike • XYZ-1234",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            
                            // Call Button
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp),
                                onClick = { /* Call Action */ }
                            ) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = "Call Rider",
                                    tint = Color.White,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(20.dp))
                        
                        // Action Buttons
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = { /* Support */ },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red)
                            ) {
                                Text("Support")
                            }
                            Button(
                                onClick = { /* Trip Details */ },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Text("Trip Details")
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Live location not available yet",
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
