package com.example.floatingflavors.app.chatbot.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.chatbot.data.ChatEntity
import com.example.floatingflavors.app.chatbot.components.TimeUtils // ✅ CORRECT IMPORT

@Composable
fun ChatBubble(message: ChatEntity) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = if (message.isUser)
            Alignment.CenterEnd else Alignment.CenterStart
    ) {

        Column(
            horizontalAlignment = if (message.isUser)
                Alignment.End else Alignment.Start
        ) {

            // 🔹 GROUP CHAT SENDER NAME
            if (!message.isUser && message.senderName != null) {
                Text(
                    text = message.senderName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (message.isUser)
                    Color(0xFF00B14F) else Color(0xFFF1F1F1),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {

                    Text(
                        text = message.text,
                        color = if (message.isUser) Color.White else Color.Black,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = TimeUtils.formatTime(message.timestamp), // ✅ FIXED
                        fontSize = 11.sp,
                        color = if (message.isUser)
                            Color.White.copy(alpha = 0.7f)
                        else Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}
