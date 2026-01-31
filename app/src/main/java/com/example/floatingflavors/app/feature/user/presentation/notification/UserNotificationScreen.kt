package com.example.floatingflavors.app.feature.user.presentation.notification

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floatingflavors.app.feature.delivery.presentation.notification.NotificationUiModel

@Composable
fun UserNotificationScreen(
    onClose: () -> Unit = {},
    viewModel: UserNotificationViewModel = viewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    Scaffold(
        topBar = {
            NotificationHeader(
                onClose = onClose, 
                count = notifications.count { !it.isRead }
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        
        if (notifications.isEmpty()) {
             Box(
                modifier = Modifier.fillMaxSize().padding(padding), 
                contentAlignment = Alignment.Center
             ) {
                Text("No new notifications", color = Color.Gray)
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
                                    .background(color, RoundedCornerShape(18.dp))
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
                            NotificationCard(
                                data = item,
                                onClick = { viewModel.markAsRead(item.id) }
                            )
                        }
                    )
                }
            }
        }
    }
}

/* ---------------- HEADER ---------------- */

@Composable
private fun NotificationHeader(onClose: () -> Unit, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
                if (count > 0) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Red, CircleShape)
                            .align(Alignment.TopEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Notification",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (count > 0) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF22C55E), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "$count NEW",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

/* ---------------- CARD ---------------- */

@Composable
private fun NotificationCard(
    data: NotificationUiModel,
    onClick: () -> Unit
) {
    val unread = !data.isRead
    val bgColor = if (unread) Color(0xFFEFFDF4) else Color.White
    val borderColor = if (unread) Color(0xFFBBF7D0) else Color(0xFFE5E7EB)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .background(bgColor, RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(16.dp)
            .clickable { onClick() },
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE0F2FE)), // Light Blue Default
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = data.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (unread) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF22C55E), CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = data.message,
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = data.time,
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
