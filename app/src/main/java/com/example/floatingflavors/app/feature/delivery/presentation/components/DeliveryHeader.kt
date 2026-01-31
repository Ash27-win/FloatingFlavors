package com.example.floatingflavors.app.feature.delivery.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

private val Orange = Color(0xFFEC6D13)
private val Muted = Color(0xFF8A8A8A)

@Composable
fun DeliveryHeader(name: String, unreadCount: Int = 0, onNotificationClick: () -> Unit = {}) {
    val date = remember {
        SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()).format(Date())
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(date, fontSize = 12.sp, color = Muted)
            Text("Hi, $name", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFF2F2F2), CircleShape)
                .clickable { onNotificationClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Notifications, null, tint = Orange)
            
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color.Red, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = 0.dp, y = 2.dp)
                        .border(1.dp, Color.White, CircleShape)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFE0E0E0), CircleShape)
        )
    }
}
