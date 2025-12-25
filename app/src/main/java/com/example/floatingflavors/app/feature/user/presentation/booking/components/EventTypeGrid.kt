package com.example.floatingflavors.app.feature.user.presentation.booking.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EventTypeGrid(
    selected: String,
    onSelect: (String) -> Unit
) {
    val items = listOf(
        "Birthday" to "🎂",
        "Wedding" to "💒",
        "Corporate" to "🏢",
        "Anniversary" to "💖",
        "Other" to "🎉"
    )

    Column {
        items.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (title, emoji) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                1.dp,
                                if (selected == title) Color(0xFF9333EA)
                                else Color(0xFFE5E7EB),
                                RoundedCornerShape(16.dp)
                            )
                            .background(Color.White)
                            .clickable { onSelect(title) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(emoji, fontSize = 22.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(title, fontSize = 10.sp)
                        }
                    }
                }
                if (row.size < 3) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}