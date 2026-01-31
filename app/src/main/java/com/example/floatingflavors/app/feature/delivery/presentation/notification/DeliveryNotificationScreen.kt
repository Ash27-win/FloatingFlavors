package com.example.floatingflavors.app.feature.delivery.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DeliveryNotificationScreen(
    viewModel: DeliveryNotificationViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val notifications by viewModel.notifications.collectAsState()
    val bgColor = Color(0xFFF8FAFC)

    Scaffold(
        topBar = {
            DeliveryNotificationHeader(onBack = onBack, count = notifications.count { !it.isRead })
        },
        containerColor = bgColor
    ) { padding ->

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No notifications", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 32.dp, top = 12.dp)
            ) {
                items(notifications, key = { it.id }) { item ->
                    
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                viewModel.delete(item.id)
                                true
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = Color.Red
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color, RoundedCornerShape(12.dp))
                                    .padding(end = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.White
                                )
                            }
                        },
                        content = {
                            DeliveryNotificationCard(
                                item = item,
                                onClick = { viewModel.markAsRead(item.id) }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeliveryNotificationHeader(onBack: () -> Unit, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Notifications",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.weight(1f))
        if (count > 0) {
            Box(
                modifier = Modifier
                    .background(Color.Red, CircleShape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$count NEW",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun DeliveryNotificationCard(
    item: NotificationUiModel,
    onClick: () -> Unit
) {
    val unread = !item.isRead
    val bgColor = if (unread) Color.White else Color(0xFFF1F5F9)
    val borderColor = if (unread) Color(0xFF22C55E) else Color.Transparent

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp))
            .border(if (unread) 1.dp else 0.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (unread) Color(0xFFDCFCE7) else Color(0xFFE2E8F0), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.type == "NEW_ORDER") Icons.Default.DirectionsBike else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = if (unread) Color(0xFF16A34A) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    if (unread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = item.message,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = item.time,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
