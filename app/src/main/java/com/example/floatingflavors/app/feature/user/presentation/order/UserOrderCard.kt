package com.example.floatingflavors.app.feature.user.presentation.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun UserOrderCard(
    order: UserOrderUiModel,
    onDetails: () -> Unit
) {
    val borderColor = if (order.isEvent) Color(0xFF3B82F6) else Color(0xFFFB923C)
    val icon = if (order.isEvent) Icons.Default.Celebration else Icons.Default.Restaurant

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row {
            Box(
                Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(borderColor)
            )

            Column(Modifier.padding(16.dp)) {

                /* HEADER */
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = borderColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(icon, null, tint = borderColor, modifier = Modifier.padding(8.dp))
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Text("#${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            formatOrderDate(order.dateTime),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    OrderStatusChip(order.status, order.isEvent)
                }

                if (order.isEvent) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Event Order",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                ),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                order.items.forEach {
                    Text(it, fontSize = 14.sp, color = Color(0xFF475569))
                }

                Divider(Modifier.padding(vertical = 12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("TOTAL", fontSize = 10.sp, color = Color.Gray)
                        Text(order.amount, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = onDetails) {
                        Text("Details", color = Color(0xFFFB923C), fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowForward, null, tint = Color(0xFFFB923C))
                    }
                }
            }
        }
    }
}

fun formatOrderDate(raw: String?): String {
    return try {
        val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val output = SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault())
        output.format(input.parse(raw ?: "")!!)
    } catch (e: Exception) {
        raw ?: ""
    }
}



