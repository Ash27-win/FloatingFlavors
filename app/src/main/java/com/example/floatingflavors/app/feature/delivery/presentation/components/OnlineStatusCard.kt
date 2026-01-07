package com.example.floatingflavors.app.feature.delivery.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Orange = Color(0xFFEC6D13)
private val Border = Color(0xFFF0E6DE)

@Composable
fun OnlineStatusCard() {
    var isOnline by rememberSaveable { mutableStateOf(true) }

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDEAF0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(
                                if (isOnline) Color(0xFF22C55E) else Color.Gray,
                                CircleShape
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isOnline) "You are Online" else "You are Offline",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    "Ready for new requests",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Switch(
                checked = isOnline,
                onCheckedChange = { isOnline = it },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = Color(0xFFEC6D13),
                    uncheckedTrackColor = Color.LightGray,
                    checkedThumbColor = Color.White
                )
            )
        }
    }
}


