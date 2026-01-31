package com.example.floatingflavors.app.feature.delivery.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.delivery.data.OrderDto

private val Orange = Color(0xFFEC6D13)
private val Border = Color(0xFFF0E6DE)

@Composable
fun UpcomingOrderCard(
    order: OrderDto,
    onClick: () -> Unit,
    onAccept: () -> Unit // ✅ Added Callback
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Order #${order.id}",
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "₹${order.amount}",
                    color = Orange,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = "${order.pickupAddress} → ${order.dropAddress}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            
            Spacer(Modifier.height(12.dp))
            
            // ✅ Accept Button
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)), // Green
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Accept Order", color = Color.White)
            }
        }
    }
}
