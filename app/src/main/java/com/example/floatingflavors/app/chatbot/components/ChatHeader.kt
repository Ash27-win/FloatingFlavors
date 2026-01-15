package com.example.floatingflavors.app.chatbot.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp   // ✅ REQUIRED

@Composable
fun ChatHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF00B14F))
            .padding(16.dp)
    ) {
        Text(
            text = "Floating Flavors AI 🤖",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Order • Track • Book",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp
        )
    }
}
