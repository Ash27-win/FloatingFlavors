package com.example.floatingflavors.app.feature.delivery.presentation.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.delivery.data.OrderDto

private val Orange = Color(0xFFEC6D13)
private val Border = Color(0xFFF0E6DE)

@Composable
fun ActiveDeliveryCard(
    order: OrderDto,
    onViewDetails: () -> Unit
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(20.dp))
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Active Delivery", fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFE9D6), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("IN PROGRESS", fontSize = 11.sp, color = Orange)
                }
            }

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFD9D9D9), RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Order #${order.id}", fontWeight = FontWeight.Medium)
                    Text("Customer: ${order.customerName}", fontSize = 13.sp, color = Color.Gray)
                }

                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Call",
                    tint = Color(0xFF22C55E),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            order.customerPhone?.let { phone ->
                                val intent = Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.parse("tel:$phone")
                                )
                                context.startActivity(intent)
                            }
                        }
                )
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onViewDetails,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Text("View Details", color = Color.White)
            }
        }
    }
}
