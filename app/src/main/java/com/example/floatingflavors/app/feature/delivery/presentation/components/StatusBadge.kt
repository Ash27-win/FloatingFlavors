package com.example.floatingflavors.app.feature.delivery.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (bgColor, textColor, icon) = when (status.uppercase()) {
        "VERIFIED", "ACTIVE" -> Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), Icons.Default.CheckCircle)
        "PENDING" -> Triple(Color(0xFFFFF3E0), Color(0xFFFF9800), Icons.Default.Pending)
        "EXPIRED" -> Triple(Color(0xFFFFEBEE), Color(0xFFF44336), Icons.Default.ErrorOutline)
        else -> Triple(Color.LightGray, Color.DarkGray, Icons.Default.Info)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(50),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = textColor, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Text(status.uppercase(), color = textColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
