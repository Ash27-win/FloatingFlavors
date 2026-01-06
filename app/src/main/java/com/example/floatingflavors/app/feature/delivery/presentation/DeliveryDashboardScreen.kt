package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.floatingflavors.app.feature.delivery.presentation.components.*
import androidx.compose.ui.unit.sp

@Composable
fun DeliveryDashboardScreen(
    viewModel: DeliveryDashboardViewModel,
    deliveryPartnerId: Int,
    onViewDetails: (Int) -> Unit
) {
    LaunchedEffect(deliveryPartnerId) {
        viewModel.loadDashboard()
    }
    val state by viewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        item { DeliveryHeader(state?.deliveryPartnerName ?: "") }
        item { OnlineStatusCard() }
        item {
            StatsRow(
                earnings = "₹85.50",
                trips = state?.upcomingOrders?.size?.toString() ?: "0"
            )
        }

        state?.activeOrder?.let {
            item {
                ActiveDeliveryCard(
                    orderId = it.id,
                    customerName = it.customerName,
                    onViewDetails = { onViewDetails(it.id) }
                )
            }
        }

        // 🔹 UPCOMING ORDERS SECTION
        if (!state?.upcomingOrders.isNullOrEmpty()) {

            item {
                Text(
                    text = "Upcoming Orders",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(state!!.upcomingOrders) { order ->
                UpcomingOrderCard(order = order)
            }
        }
    }
}









//package com.example.floatingflavors.app.feature.delivery.presentation
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun DeliveryDashboardScreen(
//    viewModel: DeliveryDashboardViewModel,
//    deliveryPartnerId: Int,
//    onViewDetails: (Int) -> Unit
//) {
//    // ✅ CORRECT FUNCTION CALL
//    LaunchedEffect(Unit) {
//        viewModel.loadDashboard()
//    }
//
//    val state by viewModel.state.collectAsState()
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF8F7F6))
//    ) {
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            contentPadding = PaddingValues(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//
//            // 🔹 HEADER
//            item {
//                Column {
//                    Text(
//                        text = "Hi, ${state?.deliveryPartnerName ?: ""}",
//                        fontSize = 22.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = "Ready for new requests",
//                        fontSize = 13.sp,
//                        color = Color.Gray
//                    )
//                }
//            }
//
//            // 🔹 ONLINE STATUS
//            item {
//                Card(
//                    shape = RoundedCornerShape(18.dp),
//                    colors = CardDefaults.cardColors(Color.White)
//                ) {
//                    Row(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Column {
//                            Text("You are Online", fontWeight = FontWeight.SemiBold)
//                            Text(
//                                "Ready for new requests",
//                                fontSize = 12.sp,
//                                color = Color.Gray
//                            )
//                        }
//                        Switch(
//                            checked = true,
//                            onCheckedChange = {}
//                        )
//                    }
//                }
//            }
//
//            // 🔹 ACTIVE DELIVERY
//            state?.activeOrder?.let { order ->
//                item {
//                    Card(
//                        shape = RoundedCornerShape(22.dp),
//                        colors = CardDefaults.cardColors(Color.White),
//                        elevation = CardDefaults.cardElevation(6.dp)
//                    ) {
//                        Column(modifier = Modifier.padding(16.dp)) {
//
//                            Text(
//                                text = "Active Delivery",
//                                fontWeight = FontWeight.Bold,
//                                fontSize = 18.sp
//                            )
//
//                            Spacer(Modifier.height(12.dp))
//
//                            Text("Order #${order.id}", fontWeight = FontWeight.Medium)
//                            Text("Customer: ${order.customerName}", color = Color.Gray)
//
//                            Spacer(Modifier.height(16.dp))
//
//                            Button(
//                                onClick = { onViewDetails(order.id) },
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(48.dp),
//                                shape = RoundedCornerShape(50)
//                            ) {
//                                Text("View Details")
//                            }
//                        }
//                    }
//                }
//            }
//
//            // 🔹 UPCOMING ORDERS
//            if (!state?.upcomingOrders.isNullOrEmpty()) {
//                item {
//                    Text(
//                        text = "Upcoming Orders",
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp
//                    )
//                }
//
//                items(state!!.upcomingOrders) { order ->
//                    Card(
//                        shape = RoundedCornerShape(18.dp),
//                        colors = CardDefaults.cardColors(Color.White)
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp)
//                        ) {
//                            Row(
//                                horizontalArrangement = Arrangement.SpaceBetween,
//                                modifier = Modifier.fillMaxWidth()
//                            ) {
//                                Text(
//                                    "Order #${order.id}",
//                                    fontWeight = FontWeight.Medium
//                                )
//                                Text(
//                                    "₹${order.amount}",
//                                    color = Color(0xFFEC6D13)
//                                )
//                            }
//
//                            Spacer(Modifier.height(6.dp))
//
//                            Text(
//                                text = "${order.pickupAddress} → ${order.dropAddress}",
//                                fontSize = 12.sp,
//                                color = Color.Gray,
//                                maxLines = 2
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
