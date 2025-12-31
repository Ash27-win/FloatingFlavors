package com.example.floatingflavors.app.feature.user.presentation.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderDetailsScreen(
    orderId: String,
    viewModel: OrderDetailsViewModel,
    onBack: () -> Unit,
    onTrack: () -> Unit
) {
    LaunchedEffect(orderId) {
        viewModel.load(orderId)
    }

    val order by viewModel.order.collectAsState()
    val address by viewModel.address.collectAsState()

    val deliveryFee = 20.0
    val gstPercent = 0.05

    val subtotal = order?.amount?.toDoubleOrNull() ?: 0.0
    val gst = subtotal * gstPercent
    val total = subtotal + gst + deliveryFee

    val formattedDate = remember(order?.created_at) {
        try {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            output.format(input.parse(order?.created_at?.substring(0, 10) ?: "")!!)
        } catch (e: Exception) {
            order?.created_at ?: "-"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        /* ---------------- HEADER ---------------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "Order Invoice",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))

        /* ---------------- ORDER SUMMARY ---------------- */
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {

                // 🔹 STATUS CHIP (TOP LEFT)
                Box(
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    OrderStatusChip(
                        status = order?.status ?: "Pending",
                        isEvent = false
                    )
                }

                // 🔹 ORDER ID + DATE (CENTER)
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Order #${order?.id ?: "-"}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        formattedDate,
                        fontSize = 13.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        /* ---------------- ORDER DETAILS ---------------- */
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Order Details", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(12.dp))

                val totalQty = order?.items?.sumOf { it.qty ?: 0 } ?: 1
                val unitPrice = subtotal / totalQty

                order?.items.orEmpty().forEach {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(it.name ?: "-", fontWeight = FontWeight.Medium)
                            Text(
                                "Qty: ${it.qty ?: 0}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            "₹${String.format("%.0f", unitPrice * (it.qty ?: 1))}",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Divider(Modifier.padding(vertical = 12.dp))

                PriceRow("Subtotal", subtotal)
                PriceRow("Delivery Fee", deliveryFee)
                PriceRow("GST (5%)", gst)

                Divider(Modifier.padding(vertical = 12.dp))

                PriceRow("Total Amount", total, true)
            }
        }

        Spacer(Modifier.height(16.dp))

        /* ---------- DELIVERY INFO ---------- */
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Delivery Information", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(10.dp))

                // 📍 Delivery Address
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Delivery Address", fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    "${address?.house ?: "-"}, ${address?.area ?: "-"}, ${address?.city ?: "-"}",
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(start = 26.dp)
                )

                Spacer(Modifier.height(12.dp))

                // 📞 Contact Number
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = null,
                        tint = Color(0xFF6B7280),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Contact Number", fontWeight = FontWeight.Medium)
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    "Available in profile",
                    color = Color(0xFF6B7280),
                    modifier = Modifier.padding(start = 26.dp)
                )
            }
        }


        /* ---------------- PAYMENT INFO ---------------- */
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Payment Information", fontWeight = FontWeight.Bold)

                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Payment Method")
                    Text("Cash on Delivery")
                }

                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Payment Status")
                    OrderStatusChip(
                        status = "Pending",
                        isEvent = false
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        /* ---------------- ACTION BUTTONS ---------------- */
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick = {}
            ) {
                Text("Download Invoice")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = onTrack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B00)
                )
            ) {
                Text("Track")
            }
        }

        Spacer(Modifier.height(16.dp))

        /* ---------------- SUPPORT ---------------- */
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(Color(0xFFEFF6FF))
        ) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Need help? Contact our support team at\nsupport@cloudkitchen.com",
                fontSize = 13.sp,
                color = Color(0xFF2563EB)
            )
        }
    }
}

@Composable
private fun PriceRow(label: String, value: Double, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF6B7280))
        Text(
            "₹${String.format("%.2f", value)}",
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
