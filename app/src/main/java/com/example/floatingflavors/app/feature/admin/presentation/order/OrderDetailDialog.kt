package com.example.floatingflavors.app.feature.admin.presentation.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrderDto

@Composable
fun OrderDetailDialog(
    order: OrderDto,
    scrimAlpha: Float = 0.45f,              // tweak to match Figma
    onDismiss: () -> Unit,
    onAccept: (orderId: String) -> Unit,
    onReject: (orderId: String) -> Unit,
    onMarkDelivered: (orderId: String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        // Full screen container so scrim covers entire device
        Box(modifier = Modifier
            .fillMaxSize()
        ) {
            // Full-screen scrim (click outside to dismiss)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .clickable { onDismiss() }
            )

            // Centered card
            Card(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.90f)    // 90% of screen width
                    .padding(12.dp),
                shape = MaterialTheme.shapes.large,
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {

                    // header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Order Details", style = MaterialTheme.typography.titleMedium)
                            Text(text = order.id ?: "#-", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // customer + phone
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Customer", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(order.customer_name ?: "-", style = MaterialTheme.typography.bodyMedium)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Phone", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text("+91 98765 43210", style = MaterialTheme.typography.bodyMedium) // replace with real phone field if available
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // address + distance
                    Text("Address", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("Sector 15, Noida", style = MaterialTheme.typography.bodyMedium) // replace if DB field exists

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(order.distance ?: "-", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ---- first horizontal divider (above items) ----
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Items list
                    Text("Order Items:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Column(modifier = Modifier.padding(start = 6.dp, top = 8.dp)) {
                        order.items?.forEach { it ->
                            Text("• ${it.name} x${it.qty}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ---- second horizontal divider (below items) ----
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    // Total + Payment method row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(order.amount ?: "-", style = MaterialTheme.typography.titleMedium)
                        }

                        // Payment chip (if you track payment method; replace with real field)
                        Surface(shape = MaterialTheme.shapes.small, tonalElevation = 0.dp, color = Color(0xFFF3F4F6)) {
                            Text(text = "COD", modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = Color(0xFF374151), style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Actions area — dynamic by status
                    when ((order.status ?: "").lowercase()) {
                        "pending" -> {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(
                                    onClick = { onAccept(order.id ?: "") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Accept", color = Color.White) }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = { onReject(order.id ?: "") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Reject", color = Color.White) }
                            }
                        }
//                        "active" -> {
//                            // single primary action to mark delivered
//                            Button(
//                                onClick = { onMarkDelivered(order.id ?: "") },
//                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Text("Mark as Delivered", color = Color.White)
//                            }
//                        }
                        "active" -> {
                            Column {
                                Button(
                                    onClick = { onAccept(order.id ?: "") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Start Delivery", color = Color.White)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Close")
                                }
                            }
                        }
                        "completed", "done" -> {
                            // completed — show only Close
                            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                                Text("Close")
                            }
                        }
                        else -> {
                            // fallback: show accept/reject
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Button(onClick = { onAccept(order.id ?: "") }, modifier = Modifier.weight(1f)) { Text("Accept") }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(onClick = { onReject(order.id ?: "") }, modifier = Modifier.weight(1f)) { Text("Reject") }
                            }
                        }
                    }
                }
            }
        }
    }
}
