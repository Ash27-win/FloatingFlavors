package com.example.floatingflavors.app.feature.user.presentation.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrderStatusChip(status: String, isEvent: Boolean) {

    val (bg, fg, icon) = when (status.lowercase()) {
        "pending", "preparing" -> Triple(
            Color(0xFFFFEDD5),
            Color(0xFFEA580C),
            Icons.Filled.Schedule
        )
        "confirmed" -> if (isEvent) {
            Triple(Color(0xFFDBEAFE), Color(0xFF2563EB), Icons.Filled.CheckCircle)
        } else {
            Triple(Color(0xFFFFEDD5), Color(0xFFEA580C), Icons.Filled.CheckCircle)
        }
        else -> Triple(
            Color(0xFFE5E7EB),
            Color(0xFF6B7280),
            Icons.Filled.Info
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(
            status.replaceFirstChar { it.uppercase() },
            color = fg,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
