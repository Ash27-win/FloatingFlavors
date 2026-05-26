package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.components.CheckoutStepper

@Composable
fun CheckoutSummaryScreen(
    vm: CheckoutSummaryViewModel,
    userId: Int,
    bookingId: Int,
    addressId: Int,
    onBack: () -> Unit,
    onChangeAddress: () -> Unit,
    onContinue: () -> Unit
) {
    var hasActiveMembership by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        vm.load(userId, bookingId, addressId)
        try {
            val response = NetworkClient.membershipApi.getMembership(userId)
            hasActiveMembership = response.currentPlan != null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val state = vm.uiState

    if (state.loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F6))
    ) {

        /* ---------------- HEADER ---------------- */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Order Summary",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
        }

        CheckoutStepper(activeStep = 2)

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            /* ---------------- DELIVERY ADDRESS ---------------- */
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Deliver to", fontWeight = FontWeight.SemiBold)
                            Text(
                                "Change",
                                color = Color(0xFF2563EB),
                                modifier = Modifier.clickable { onChangeAddress() }
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        state.address?.let { addr ->
                            Text(addr.label, fontWeight = FontWeight.Bold)
                            Text(
                                "${addr.house}, ${addr.area}, ${addr.city} - ${addr.pincode}",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            /* ---------------- EVENT DETAILS ---------------- */
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        Text("Event Information", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))

                        state.event?.let { event ->
                            SummaryIconRow(
                                icon = Icons.Default.Celebration,
                                label = "Type",
                                value = event.event_type
                            )
                            SummaryIconRow(
                                icon = Icons.Default.CalendarToday,
                                label = "Date",
                                value = event.event_date
                            )
                            SummaryIconRow(
                                icon = Icons.Default.Group,
                                label = "Guests",
                                value = event.people_count
                            )
                        }
                    }
                }
            }

            /* ---------------- SELECTED DISHES ---------------- */
            item {
                Row(
                    modifier = Modifier.padding(start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = Color(0xFF2563EB),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Selected Dishes",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            items(state.items) { item ->
                DishRow(
                    name = item.name,
                    quantity = item.quantity,
                    price = item.total
                )
            }

            /* ---------------- PROMO TIP CARD ---------------- */
            if (!hasActiveMembership) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E8)),
                        border = BorderStroke(1.dp, Color(0xFFFF6B00))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "💡 Tip: You can save 20% on this booking by joining the Elite membership on the payment screen!",
                                color = Color(0xFF111111),
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            /* ---------------- PRICE DETAILS ---------------- */
            item {
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFE5E7EB)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        PriceRow("Subtotal", state.subtotal)
                        PriceRow("GST (5%)", state.gst)
                        PriceRow("Delivery Fee", state.deliveryFee)

                        Divider(Modifier.padding(vertical = 8.dp))

                        PriceRow(
                            label = "Total",
                            value = state.total,
                            bold = true
                        )
                    }
                }
            }
        }

        /* ---------------- BOTTOM CTA ---------------- */
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                .height(56.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFACC15) // Figma yellow
            )
        ) {
            Text(
                "Continue",
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

/* ==================== SMALL UI COMPONENTS ==================== */

@Composable
private fun SummaryIconRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(label, modifier = Modifier.weight(1f), color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DishRow(
    name: String,
    quantity: String,
    price: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium)
            Text(
                "x $quantity sets",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        Text(
            "₹$price",
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PriceRow(
    label: String,
    value: Double,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            "₹%.2f".format(value),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}
