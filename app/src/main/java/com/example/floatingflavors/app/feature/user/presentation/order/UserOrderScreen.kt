package com.example.floatingflavors.app.feature.user.presentation.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.core.navigation.Screen

@Composable
fun UserOrdersScreen(viewModel: UserOrdersViewModel, onOpenOrderDetails: (String) -> Unit) {

    var tab by remember { mutableStateOf(0) }
    val active by viewModel.activeOrders.collectAsState()
    val past by viewModel.pastOrders.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(top = 16.dp)
    ) {

        Text(
            "My Orders",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Text(
            "Track your orders and view history",
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        Spacer(Modifier.height(16.dp))

        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(4.dp)
        ) {
            SegTab("Active (${active.size})", tab == 0) { tab = 0 }
            SegTab("Past Orders (${past.size})", tab == 1) { tab = 1 }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            items(if (tab == 0) active else past) {
                UserOrderCard(order = it) {
                    onOpenOrderDetails(it.orderId)
                }
            }
        }
    }
}

/* ---------- SEGMENT TAB (CORRECT WEIGHT USAGE) ---------- */
@Composable
private fun RowScope.SegTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(
                if (selected) Color(0xFFFF6B00) else Color.Transparent,
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF64748B),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
