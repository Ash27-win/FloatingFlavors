package com.example.floatingflavors.app.feature.admin.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AdminNotificationScreen() {

    val pageBg = Color(0xFFF2F2F7)
    val headerBlue = Color(0xFF2563EB) // 🔵 normal admin blue

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
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(Color.White, CircleShape)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "3",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = headerBlue
                        )
                    }
                }
            }
        }

        /* ---------- CONTENT ---------- */

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            item {
                SectionHeader(title = "New Notifications", count = 3)
            }

            item {
                AdminNotificationCard(
                    title = "New Order Received",
                    message = "You received 10 new orders in the last 30 mins.",
                    time = "5 mins ago",
                    icon = Icons.Default.ShoppingBag,
                    iconTint = Color(0xFF38BDF8),
                    unread = true
                )
            }

            item {
                AdminNotificationCard(
                    title = "Low Stock Alert",
                    message = "Veg Biryani is below safe limit. Only 3 portions left.",
                    time = "15 mins ago",
                    icon = Icons.Default.Warning,
                    iconTint = Color(0xFFF97316),
                    unread = true
                )
            }

            item {
                AdminNotificationCard(
                    title = "AI Tip",
                    message = "Offer 10% off on Pasta to boost weekday orders.",
                    time = "1 hour ago",
                    icon = Icons.Default.AutoAwesome,
                    iconTint = Color(0xFF6366F1),
                    unread = true,
                    isBlue = true
                )
            }

            item {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Earlier",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                AdminNotificationCard(
                    title = "Bulk Order Received",
                    message = "Corporate event booking for 50 people on Nov 10.",
                    time = "5 hours ago",
                    icon = Icons.Default.ShoppingBag,
                    iconTint = Color(0xFF38BDF8),
                    unread = false
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
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
    isBlue: Boolean = false
) {
    val bgColor = if (isBlue) Color(0xFFDDEBF5) else Color(0xFFF6E8E8)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .background(bgColor, RoundedCornerShape(16.dp))
            .border(1.dp, Color(0x22000000), RoundedCornerShape(16.dp))
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
