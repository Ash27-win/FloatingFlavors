package com.example.floatingflavors.app.feature.delivery.presentation.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.delivery.presentation.UpcomingOrderState

private val Orange = Color(0xFFEC6D13)
private val Border = Color(0xFFF0E6DE)

@Composable
fun UpcomingOrderCard(order: UpcomingOrderState) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(16.dp))
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Order #${order.id}", fontWeight = FontWeight.Medium)
                Text("₹${order.amount}", color = Orange, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                "${order.pickupAddress} → ${order.dropAddress}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}
