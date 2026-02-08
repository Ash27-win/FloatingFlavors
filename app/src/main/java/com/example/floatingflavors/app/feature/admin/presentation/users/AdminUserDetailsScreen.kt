package com.example.floatingflavors.app.feature.admin.presentation.users

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.admin.data.remote.AdminApi
import com.example.floatingflavors.app.feature.admin.data.remote.AdminRepository
import com.example.floatingflavors.app.feature.admin.data.remote.UserDetailsResponse

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminUserDetailsScreen(
    navController: NavController,
    userId: String
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    var details by remember { mutableStateOf<UserDetailsResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch Data
    LaunchedEffect(userId) {
        try {
            val api = NetworkClient.retrofit.create(AdminApi::class.java)
            val repository = AdminRepository(api)
            val res = repository.getUserDetails(userId)
            
            if (res != null && res.success) {
                details = res
            } else {
                errorMessage = "Failed to load user details"
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF00C853)
                )
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                details?.let { data ->
                    UserDetailsContent(data, onCall = {
                        val phone = data.user.phone
                        if (!phone.isNullOrBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                            context.startActivity(intent)
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun UserDetailsContent(details: UserDetailsResponse, onCall: () -> Unit) {
    val user = details.user
    val stats = details.stats
    val isDelivery = user.role == "Delivery"
    val primaryColor = if (isDelivery) Color(0xFFFF9800) else Color(0xFF2196F3)

    // Full Screen Image State
    var showFullImage by remember { mutableStateOf(false) }

    if (showFullImage && !user.profile_image_full.isNullOrBlank()) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showFullImage = false }) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { showFullImage = false }, // Click anywhere to close
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.profile_image_full)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Full Profile Photo",
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit, // Fit to screen
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp, start = 16.dp, end = 16.dp), // Reduced top padding
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // --- 1. PREMIUM HEADER CARD ---
        item {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { visible = true }
            
            androidx.compose.animation.AnimatedVisibility(
                visible = visible,
                enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(600)) + 
                        androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(600)) { -it / 2 }
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box {
                        // Background decorative element (Subtle Gradient)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(primaryColor.copy(alpha = 0.15f), Color.Transparent)
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 24.dp, start = 16.dp, end = 16.dp), // Reduced top padding
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar with Pulse Effect
                            val infiniteTransition = rememberInfiniteTransition()
                            val pulseScale by infiniteTransition.animateFloat(
                                initialValue = 1f,
                                targetValue = 1.05f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )

                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .scale(pulseScale)
                                    .border(4.dp, Color.White, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF5F5F5))
                                    .clickable { if (!user.profile_image_full.isNullOrBlank()) showFullImage = true },
                                contentAlignment = Alignment.Center
                            ) {
                                if (!user.profile_image_full.isNullOrBlank()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(user.profile_image_full)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Profile Photo",
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    // Initials Fallback
                                    Text(
                                        text = user.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                                        fontSize = 48.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryColor,
                                        style = MaterialTheme.typography.displayMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif)
                                    )
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(Modifier.height(8.dp))
                            
                            // Role Pill
                            Surface(
                                color = primaryColor.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = user.role.uppercase(),
                                    color = primaryColor,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                            
                            Spacer(Modifier.height(24.dp))

                            // Action Buttons (Responsive Row)
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val phone = user.phone
                                if (!phone.isNullOrBlank()) {
                                    Button(
                                        onClick = onCall,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        modifier = Modifier.weight(1f),
                                        elevation = ButtonDefaults.buttonElevation(2.dp)
                                    ) {
                                        Icon(Icons.Default.Call, null, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Call", maxLines = 1, style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                                
                                val context = LocalContext.current
                                Button(
                                    onClick = { 
                                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:${user.email}")
                                            putExtra(Intent.EXTRA_SUBJECT, "Support from FloatingFlavors")
                                        }
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Handle no existing email app
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = primaryColor.copy(alpha = 0.1f), 
                                        contentColor = primaryColor
                                    ),
                                    modifier = Modifier.weight(1f),
                                    elevation = ButtonDefaults.buttonElevation(0.dp)
                                ) {
                                    Icon(Icons.Default.Email, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Email", maxLines = 1, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- 2. STATS OVERVIEW ---
        item {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(100); visible = true }
             
            androidx.compose.animation.AnimatedVisibility(
                visible = visible,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandVertically()
            ) {
                Row(
                   Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        title = "Total Orders",
                        value = stats.total_orders.toString(),
                        icon = Icons.Default.ShoppingBag,
                        color = Color(0xFFEC407A), // Pinkish
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = if (isDelivery) "Earnings" else "Total Spent",
                        value = "₹${stats.total_spent.toInt()}",
                        icon = Icons.Default.AttachMoney,
                        color = Color(0xFF26A69A), // Teal/Green
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 3. CONTACT INFO ---
        item {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { delay(200); visible = true }

            androidx.compose.animation.AnimatedVisibility(
                visible = visible,
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInHorizontally { it / 4 }
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(
                            "Contact Info", 
                            style = MaterialTheme.typography.titleMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        if (!user.phone.isNullOrBlank()) {
                            InfoRow(Icons.Default.Phone, "Phone Number", user.phone)
                            Divider(Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha=0.2f))
                        }
                        
                        InfoRow(Icons.Default.Email, "Email Address", user.email)
                        Divider(Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha=0.2f))
                        
                        InfoRow(Icons.Default.DateRange, "Member Since", user.created_at ?: "Unknown")
                    }
                }
            }
        }

        // --- 4. ADDRESSES ---
        if (details.addresses.isNotEmpty()) {
            item {
                Text(
                    "Locations",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
                )
            }
            itemsIndexed(details.addresses) { index, addr ->
                // Staggered Animation for list items
                var itemVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { 
                    delay(300L + (index * 100L)) 
                    itemVisible = true 
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = itemVisible,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 }
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                Modifier.size(48.dp).background(Color(0xFFEEEEEE), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = Color(0xFF757575))
                            }
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = addr.label ?: "Address",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = addr.full_address ?: "No details available",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = modifier
    ) {
        Column(
            Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(text = value, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
            Text(text = title, fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color(0xFF757575), modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(20.dp))
        Column {
             Text(text = label, fontSize = 12.sp, color = Color.Gray)
             Text(text = value, fontSize = 16.sp, color = Color.Black, fontWeight = FontWeight.Medium)
        }
    }
}
