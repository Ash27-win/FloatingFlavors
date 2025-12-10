package com.example.floatingflavors.app.feature.order.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrderDto

@Composable
fun OrderListCard(
    order: OrderDto,
    timeText: String,
    onStatusChange: (orderId: String, newStatus: String) -> Unit // Kept for compatibility but unused
) {
    // var menuExpanded by remember { mutableStateOf(false) } // Removed state

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // ------------------ HEADER -------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = order.id ?: "-",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = order.customer_name ?: "-",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Only StatusChip remains in the right header section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(order.status ?: "pending")

                    // --- REMOVED DROPDOWN MENU AND ICON BUTTON ---
                    /*
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Actions")
                    }

                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Mark Pending") }, onClick = {
                            menuExpanded = false
                            onStatusChange(order.id ?: "", "pending")
                        })
                        DropdownMenuItem(text = { Text("Mark Active") }, onClick = {
                            menuExpanded = false
                            onStatusChange(order.id ?: "", "active")
                        })
                        DropdownMenuItem(text = { Text("Mark Completed") }, onClick = {
                            menuExpanded = false
                            onStatusChange(order.id ?: "", "completed")
                        })
                    }
                    */
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // ------------------ ITEMS LIST -------------------
            order.items?.forEach { item ->
                Text(
                    text = "• ${item.name} x${item.qty}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF555555),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider()
            Spacer(modifier = Modifier.height(10.dp))

            // ------------------ FOOTER -------------------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = order.distance ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = order.amount ?: "-",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bg, textColor) = when (status.lowercase()) {
        "active" -> Color(0xFF2563EB) to Color(0xFF2563EB)
        "completed" -> Color(0xFF059669) to Color(0xFF059669)
        else -> Color(0xFFE07B00) to Color(0xFFE07B00)
    }

    Surface(
        color = bg.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}