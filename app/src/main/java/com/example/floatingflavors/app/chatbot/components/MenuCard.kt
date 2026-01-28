package com.example.floatingflavors.app.chatbot.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto

@Composable
fun MenuCard(item: MenuItemDto) {

    // ✅ SAFELY PICK IMAGE
    val imageUrl = item.image_full ?: item.image_url

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row {

            AsyncImage(
                model = imageUrl,
                contentDescription = item.name ?: "Menu Image",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = item.name ?: "",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = item.description ?: "",
                    maxLines = 2
                )

                Text(
                    text = "₹${item.price ?: "0"}",
                    color = Color(0xFF00B14F)
                )
            }
        }
    }
}
