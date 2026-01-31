package com.example.floatingflavors.app.feature.admin.presentation.notification

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
import com.example.floatingflavors.app.feature.delivery.presentation.notification.NotificationUiModel

@Composable
fun AdminNotificationScreen(
    viewModel: AdminNotificationViewModel = viewModel()
) {

    val notifications by viewModel.notifications.collectAsState()
    val pageBg = Color(0xFFF2F2F7)
    val headerBlue = Color(0xFF2563EB)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBg)
    ) {

        /* ---------- HEADER ---------- */

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .background(
                    headerBlue,
                    shape = RoundedCornerShape(
                        bottomStart = 28.dp,
                        bottomEnd = 28.dp
                    )
                )
                .padding(horizontal = 20.dp, vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Notifications",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Stay updated with your kitchen",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Box {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    val unreadCount = notifications.count { !it.isRead }
                    if (unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color.White, CircleShape)
                                .align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadCount.toString(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = headerBlue
                            )
                        }
                    }
                }
            }
        }

        /* ---------- CONTENT ---------- */

        if (notifications.isEmpty()) {
             Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notifications yet", color = Color.Gray)
             }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                
                // Show "New" Header if any unread
                if (notifications.any { !it.isRead }) {
                    item {
                        SectionHeader(title = "New Notifications", count = notifications.count { !it.isRead })
                    }
                }

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
                                    .background(color, RoundedCornerShape(16.dp))
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
                            AdminNotificationCard(
                                title = item.title,
                                message = item.message,
                                time = item.time,
                                icon = if (item.type == "ALERT") Icons.Default.Warning else Icons.Default.ShoppingBag,
                                iconTint = if (item.type == "ALERT") Color(0xFFF97316) else Color(0xFF38BDF8),
                                unread = !item.isRead,
                                onClick = { viewModel.markAsRead(item.id) }
                            )
                        }
                    )
                }
            }
        }
    }
}

/* ---------- SECTION HEADER ---------- */

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 14.sp, color = Color.Gray)
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(Color.Black, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/* ---------- CARD ---------- */

@Composable
private fun AdminNotificationCard(
    title: String,
    message: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    unread: Boolean,
    isBlue: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor = if (unread) Color.White else Color(0xFFF6E8E8) // Highlight unread
    
    // Using Card for click + shadow
    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.weight(1f)
                    )
                    if (unread) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color.Red, CircleShape)
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color(0xFF4B5563),
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(time, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
