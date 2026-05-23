package com.example.floatingflavors.app.feature.delivery.presentation.notifications

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryNotificationDto

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DeliveryNotificationScreen(
    navController: NavController,
    viewModel: DeliveryNotificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState is DeliveryNotificationUiState.Success) {
                        val unreadCount = (uiState as DeliveryNotificationUiState.Success).unreadCount
                        if (unreadCount > 0) {
                            TextButton(onClick = { viewModel.markAllAsRead() }) {
                                Text("Mark all read", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (uiState) {
                is DeliveryNotificationUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DeliveryNotificationUiState.Error -> {
                    Text(
                        (uiState as DeliveryNotificationUiState.Error).message,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is DeliveryNotificationUiState.Success -> {
                    val notifications = (uiState as DeliveryNotificationUiState.Success).notifications
                    if (notifications.isEmpty()) {
                        EmptyNotificationView()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 80.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(
                                items = notifications,
                                key = { it.id }
                            ) { notif ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.EndToStart) {
                                            viewModel.deleteNotification(notif.id)
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    enableDismissFromStartToEnd = false,
                                    backgroundContent = {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.Red)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                                        }
                                    }
                                ) {
                                    NotificationItem(
                                        notification = notif,
                                        onClick = {
                                            if (notif.isRead == 0) viewModel.markAsRead(notif.id)
                                            handleNotificationClick(navController, notif)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: DeliveryNotificationDto, onClick: () -> Unit) {
    val isUnread = notification.isRead == 0
    val bgColor by animateColorAsState(if (isUnread) Color(0xFFFFF3E0) else Color.White)

    val (icon, iconTint, iconBg) = getNotificationStyle(notification.type ?: "")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 2.dp else 0.5.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title ?: "Notification",
                    fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body ?: "",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notification.timeAgo ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            if (isUnread) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF57C00))
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

fun getNotificationStyle(type: String): Triple<ImageVector, Color, Color> {
    return when (type) {
        "ORDER_ASSIGNED", "NEW_ORDER" -> Triple(Icons.Default.Moped, Color(0xFF1976D2), Color(0xFFE3F2FD))
        "COMPLIANCE_ALERT" -> Triple(Icons.Default.GppBad, Color(0xFFD32F2F), Color(0xFFFFEBEE))
        "DOCUMENT_REJECTED" -> Triple(Icons.Default.FolderOff, Color(0xFFF57C00), Color(0xFFFFF3E0))
        "ORDER_CANCELLED" -> Triple(Icons.Default.Cancel, Color(0xFFD32F2F), Color(0xFFFFEBEE))
        "PAYMENT" -> Triple(Icons.Default.Payments, Color(0xFF388E3C), Color(0xFFE8F5E9))
        else -> Triple(Icons.Default.Notifications, Color(0xFF757575), Color(0xFFEEEEEE))
    }
}

@Composable
fun EmptyNotificationView() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("No Notifications", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Text("You're all caught up!", fontSize = 14.sp, color = Color.LightGray)
    }
}

fun handleNotificationClick(navController: NavController, notification: DeliveryNotificationDto) {
    when (notification.type) {
        "ORDER_ASSIGNED", "NEW_ORDER" -> {
            notification.referenceId?.let {
                navController.navigate("delivery_order_details/$it")
            }
        }
        "COMPLIANCE_ALERT" -> navController.navigate("delivery_vehicle_info")
        "DOCUMENT_REJECTED" -> navController.navigate("delivery_documents")
        // Add more deep links as needed
    }
}
