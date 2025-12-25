package com.example.floatingflavors.app.feature.user.presentation.booking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ToggleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
//            .weight(1f)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) Color(0xFF9333EA) else Color(0xFFE5E7EB),
                shape = RoundedCornerShape(18.dp)
            )
            .background(
                if (selected) Color(0xFFF3E8FF) else Color.White,
                RoundedCornerShape(18.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (selected) Color(0xFF9333EA) else Color.Gray,
            modifier = Modifier.size(32.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(subtitle, fontSize = 10.sp, color = Color.Gray)
    }
}
