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
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.delivery.presentation.components.*

@Composable
fun DeliveryDashboardScreen(
    viewModel: DeliveryDashboardViewModel,
    onViewDetails: (Int) -> Unit,
    onLiveTracking: (String) -> Unit, // ✅ NEW param for navigation
    onNotificationClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }
    
    // ✅ Navigation Handler
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { orderId ->
            onLiveTracking(orderId)
        }
    }

    val state by viewModel.state.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F7F6)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        item { 
            DeliveryHeader(
                name = state?.deliveryPartnerName ?: "",
                unreadCount = unreadCount,
                onNotificationClick = onNotificationClick
            ) 
        }
        item { OnlineStatusCard() }

        item {
            StatsRow(
                earnings = "₹85.50",
                trips = state?.upcomingOrders?.size?.toString() ?: "0"
            )
        }

        state?.activeOrder?.let { order ->
            item {
                ActiveDeliveryCard(
                    order = order,
                    onViewDetails = { onViewDetails(order.id) }
                )
            }
        }

        if (!state?.upcomingOrders.isNullOrEmpty()) {
            item {
                Text(
                    text = "Upcoming Orders",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(state!!.upcomingOrders) { order ->
                UpcomingOrderCard(
                    order = order,
                    onClick = {
                        // ✅ FIXED: use callback (NO navController here)
                        onViewDetails(order.id)
                    },
                    onAccept = {
                        viewModel.acceptOrder(order.id)
                    }
                )
            }
        }
    }
}
