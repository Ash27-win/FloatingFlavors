package com.example.floatingflavors.app.feature.user.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.example.floatingflavors.app.feature.user.data.remote.dto.HomeResponseDto
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.floatingflavors.app.chatbot.ChatScreen
import com.example.floatingflavors.app.chatbot.model.ChatViewModel
import com.example.floatingflavors.app.core.auth.TokenManager
import com.example.floatingflavors.app.core.navigation.Screen
import com.example.floatingflavors.app.feature.user.presentation.home.HomeUiState

/**
 * UserHomeScreen (Material3 friendly) - no token logic here.
 *
 * Expects:
 *  - UserHomeViewModel that exposes uiState: StateFlow<HomeUiState>
 *  - HomeResponseDto containing: userStats { userName, totalOrders, loyaltyPoints },
 *    featured: List<MenuItemDto>, offer/banner object with title & subtitle
 *
 * Call: UserHomeScreen(onBrowseMenu = {}, onBookingCatering = {}, onAddToCart = {})
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    viewModel: UserHomeViewModel = viewModel(),
    chatViewModel: ChatViewModel,
    navController: NavController,
    onBrowseMenu: () -> Unit = {},
    onBookingCatering: () -> Unit = {},
    onAddToCart: (Int) -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenMembership: () -> Unit = {},
    onLogout: () -> Unit = {}
)

 {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        if (uiState is HomeUiState.Idle || uiState is HomeUiState.Error) viewModel.loadHome()
    }

    val pageBg = Color(0xFFEBEBEB)
     var showChat by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = pageBg,
        topBar = {
            // Simple custom top bar (Material3 TopAppBar can be used, but this is safe)
            Surface(tonalElevation = 0.dp, color = pageBg, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when (uiState) {
                                is HomeUiState.Success -> "Hello, ${(uiState as HomeUiState.Success).data.userStats.userName}"
                                else -> "Hello, User!"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "What would you like to order today?",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onOpenMembership) {
                            Icon(imageVector = Icons.Default.CardMembership, contentDescription = "Membership")
                        }
                        IconButton(onClick = onOpenNotifications) {
                            Icon(imageVector = Icons.Outlined.NotificationsNone, contentDescription = "Notifications")
                        }
                        IconButton(onClick = { /* profile */ }) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is HomeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is HomeUiState.Error -> {
                    val msg = (uiState as HomeUiState.Error).message
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Failed to load home data")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.refresh() }) { Text("Retry") }
                        }
                    }
                }

                is HomeUiState.Success -> {
                    UserHomeContent(
                        data = (uiState as HomeUiState.Success).data,
                        onBrowseMenu = onBrowseMenu,
                        onBookingCatering = onBookingCatering,
                        onAddToCart = onAddToCart,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Button(onClick = { viewModel.loadHome() }) { Text("Load") }
                    }
                }
            }

            /* 🔥 AI CHAT FAB */
            FloatingActionButton(
                onClick = { showChat = true },
                containerColor = Color(0xFF00B14F),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = "AI Assistant"
                )
            }

            /* 🔽 CHAT BOTTOM SHEET */
            if (showChat) {
                ModalBottomSheet(
                    onDismissRequest = { showChat = false },
                    sheetState = rememberModalBottomSheetState(
                        skipPartiallyExpanded = true
                    )
                ) {
                    ChatScreen(
                        userId = com.example.floatingflavors.app.core.UserSession.userId,
                        viewModel = chatViewModel
                    )
                }
            }
        }
    }
}

@Composable
private fun UserHomeContent(
    data: HomeResponseDto,
    onBrowseMenu: () -> Unit,
    onBookingCatering: () -> Unit,
    onAddToCart: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoCard(title = "Total Orders", value = data.userStats.totalOrders.toString(), modifier = Modifier.weight(1f))
                InfoCard(title = "Loyalty Points", value = data.userStats.loyaltyPoints.toString(), modifier = Modifier.weight(1f))
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text("Quick Actions", fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onBrowseMenu,
                    modifier = Modifier.weight(1f).height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Menu, contentDescription = "Browse", tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Browse Menu", color = Color.White)
                }

                OutlinedButton(
                    onClick = onBookingCatering,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Booking")
                    Spacer(Modifier.width(8.dp))
                    Text("Booking")
                }
            }
        }

        item {
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Featured Items", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text("See All", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { /* navigate */ })
            }
            Spacer(Modifier.height(8.dp))
        }

        items(data.featured) { item ->
            FeaturedItemCard(item = item, onAddToCart = onAddToCart)
        }

        item {
            Spacer(Modifier.height(10.dp))
            OfferCard(
                title = data.offer?.title ?: "Limited Time Offer!",
                subtitle = data.offer?.subtitle ?: "Get 20% off on catering orders above ₹5000"
            )
            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun InfoCard(title: String, value: String, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.height(92.dp), shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(title, color = Color.Gray, fontSize = 13.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
    }
}

@Composable
private fun FeaturedItemCard(item: MenuItemDto, onAddToCart: (Int) -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize()) {
                val imageUrl = item.image_url?.trim()
                val fullUrl = when {
                    imageUrl.isNullOrEmpty() -> null
                    imageUrl.startsWith("http", ignoreCase = true) -> imageUrl
                    else -> NetworkClient.BASE_URL + imageUrl
                }

                if (fullUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(fullUrl).crossfade(true).build(),
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(110.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .fillMaxHeight()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No image")
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f).padding(vertical = 10.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(item.name ?: "", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            val ratingText = item.rating?.toString() ?: "4.8"
                            Text(ratingText, fontSize = 13.sp, color = Color.DarkGray)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(item.category ?: "", maxLines = 2, color = Color.Gray, fontSize = 13.sp)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("₹${item.price ?: "0"}", fontWeight = FontWeight.Bold, color = Color(0xFF00A86B), fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(90.dp))
                    }
                }
            }

            // Add button — aligned to right inside card
            val itemId = item.id?.toString()?.toIntOrNull() ?: 0
            Button(
                onClick = { onAddToCart(itemId) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F), contentColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .height(42.dp)
            ) {
                Text("Add")
            }
        }
    }
}

@Composable
private fun OfferCard(title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF00B14F))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccessTime,
            contentDescription = "Offer time",
            tint = Color.White,
            modifier = Modifier.size(36.dp)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, fontSize = 13.sp, color = Color.White)
        }

        Button(
            onClick = { /* CTA */ },
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF00B14F)
            ),
            modifier = Modifier.height(40.dp)
        ) {
            Text("Book Now")
        }
    }
}

