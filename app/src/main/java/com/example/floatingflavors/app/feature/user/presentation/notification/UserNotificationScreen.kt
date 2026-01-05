package com.example.floatingflavors.app.feature.user.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserNotificationScreen(
    onClose: () -> Unit = {}
) {
    val notifications = listOf(
        NotificationUi(
            title = "25% OFF on Burgers!",
            message = "Get flat 25% discount on all burger combos. Valid till tonight.",
            time = "2 hours ago",
            icon = Icons.Default.Percent,
            iconBg = Color.White,
            iconTint = Color(0xFF22C55E),
            highlighted = true,
            unread = true
        ),
        NotificationUi(
            title = "Free Delivery Nearby",
            message = "Free delivery on orders above ₹199 within 3 km radius.",
            time = "5 hours ago",
            icon = Icons.Default.LocalShipping,
            iconBg = Color.White,
            iconTint = Color(0xFFF97316),
            highlighted = true,
            unread = false
        ),
        NotificationUi(
            title = "Membership Renewal",
            message = "Your monthly membership expires in 3 days. Renew now to continue enjoying benefits!",
            time = "1 day ago",
            icon = Icons.Default.Diamond,
            iconBg = Color(0xFFF3E8FF),
            iconTint = Color(0xFF9333EA)
        ),
        NotificationUi(
            title = "New Dish Added",
            message = "Try our new Butter Paneer Masala – now available!",
            time = "2 days ago",
            icon = Icons.Default.LunchDining,
            iconBg = Color(0xFFFFEDD5),
            iconTint = Color(0xFFF97316)
        ),
        NotificationUi(
            title = "Rate your order",
            message = "How was your Spicy Chicken Wings? Rate us to help us improve.",
            time = "3 days ago",
            icon = Icons.Default.Star,
            iconBg = Color(0xFFEFF6FF),
            iconTint = Color(0xFF3B82F6)
        )
    )

    Scaffold(
        topBar = {
            NotificationHeader(onClose)
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 12.dp)
        ) {
            items(notifications) {
                NotificationCard(it)
            }
        }
    }
}

/* ---------------- HEADER ---------------- */

@Composable
private fun NotificationHeader(onClose: () -> Unit) {
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
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Notification",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .background(Color(0xFF22C55E), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "2 NEW",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "Close")
        }
    }
}

/* ---------------- CARD ---------------- */

@Composable
private fun NotificationCard(data: NotificationUi) {
    val bgColor =
        if (data.highlighted) Color(0xFFEFFDF4) else Color.White
    val borderColor =
        if (data.highlighted) Color(0xFFBBF7D0) else Color(0xFFE5E7EB)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp))
            .background(bgColor, RoundedCornerShape(18.dp))
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(data.iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = data.icon,
                contentDescription = null,
                tint = data.iconTint,
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
                if (data.unread) {
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

/* ---------------- MODEL ---------------- */

private data class NotificationUi(
    val title: String,
    val message: String,
    val time: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val highlighted: Boolean = false,
    val unread: Boolean = false
)
