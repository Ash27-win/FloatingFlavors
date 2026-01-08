package com.example.floatingflavors.app.feature.admin.presentation.orders

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floatingflavors.MainActivity
import com.example.floatingflavors.app.feature.admin.presentation.order.BookingDetailDialog
import com.example.floatingflavors.app.feature.admin.presentation.order.OrderDetailDialog
import com.example.floatingflavors.app.feature.admin.presentation.order.RejectReasonDialog // Add this import
import com.example.floatingflavors.app.feature.admin.presentation.tracking.AdminPermissionHandler
import com.example.floatingflavors.app.feature.admin.presentation.tracking.service.LocationUpdateService
import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrderDto
import com.example.floatingflavors.app.feature.orders.presentation.OrdersViewModel
import com.example.floatingflavors.app.feature.order.component.OrderListCard
import com.example.floatingflavors.app.util.computeTimeAgoFromIso
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Make TabLabel public so it can be used in public signatures
data class TabLabel(val title: String, val count: Int)

// Store orderId for permission callback
var pendingOrderIdForTracking = mutableStateOf<String?>(null)

// Add state for reject reason dialog
var showRejectReasonDialog = mutableStateOf(false)
var orderIdToReject = mutableStateOf<String?>(null)

// WAKE_LOCK permission check function
private fun hasWakeLockPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.WAKE_LOCK
    ) == PackageManager.PERMISSION_GRANTED
}

/** Compact search bar used under the header */
@Composable
fun OrdersSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Search orders…") },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp),
        singleLine = true,
        shape = MaterialTheme.shapes.large
    )
}

/**
 * Admin Orders screen (responsive, no top gap above header).
 */
@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdminOrdersScreen(vm: OrdersViewModel = viewModel()) {
    // VM state
    val orders by vm.orders.collectAsState()
    val filteredOrders by vm.filteredOrders.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()
    val counts by vm.tabCounts.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()
    val selectedTabState by vm.selectedTab.collectAsState()
    val selectedOrder by vm.selectedOrder.collectAsState()
    val context = LocalContext.current
    val activity = context as? MainActivity

    // Live times map (id -> time_ago text)
    var liveTimes by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val selectedBooking by vm.selectedBooking.collectAsState()

    // Local state for reject reason dialog
    var showRejectDialog by remember { mutableStateOf(false) }
    var pendingOrderIdForRejection by remember { mutableStateOf<String?>(null) }

    if (selectedBooking != null) {
        BookingDetailDialog(
            booking = selectedBooking!!,
            onAccept = {
                val bookingId = selectedBooking!!.id?.toString() ?: ""
                vm. updateBookingStatus(bookingId, "CONFIRMED")
                vm.loadOrders()
            },
            onReject = {
                val bookingId = selectedBooking!!.id?.toString() ?: ""
                vm.updateBookingStatus(bookingId, "CANCELLED")
                vm.loadOrders()
            },
            onDismiss = { vm.clearBooking() }
        )
    }

    // Show reject reason dialog
    if (showRejectDialog && pendingOrderIdForRejection != null) {
        RejectReasonDialog(
            onDismiss = {
                showRejectDialog = false
                pendingOrderIdForRejection = null
            },
            onConfirm = { reason ->
                pendingOrderIdForRejection?.let { orderId ->
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Rejecting order...")

                        vm.rejectOrderWithReason(orderId, reason) { success, err ->
                            coroutineScope.launch {
                                if (success) {
                                    // ✅ SUCCESS - Show correct message
                                    snackbarHostState.showSnackbar("Order rejected successfully")

                                    // ✅ Refresh the entire orders list
                                    vm.loadOrders()

                                    // ✅ Close the reject dialog
                                    showRejectDialog = false
                                    pendingOrderIdForRejection = null

                                    // ✅ Also close the main order detail dialog
                                    vm.clearSelectedOrder()
                                } else {
                                    // ❌ FAILURE
                                    snackbarHostState.showSnackbar("Failed to reject: ${err ?: "Unknown error"}")
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    // initial load
    LaunchedEffect(Unit) { vm.loadOrders() }

    // update liveTimes on orders change
    LaunchedEffect(orders) {
        liveTimes = orders.associate { order ->
            val idKey: String = order.id ?: ""
            val timeValue: String = order.time_ago ?: computeTimeAgoFromIso(order.created_at)
            idKey to timeValue
        }
    }

    // periodic update for "time ago"
    LaunchedEffect(orders) {
        while (isActive) {
            liveTimes = orders.associate { order ->
                val idKey: String = order.id ?: ""
                val timeValue: String = order.time_ago ?: computeTimeAgoFromIso(order.created_at)
                idKey to timeValue
            }
            delay(30_000L)
        }
    }

    // Build explicit TabLabel list
    val labels: List<TabLabel> = listOf(
        TabLabel("All", counts["all"] ?: 0),
        TabLabel("Pending", counts["pending"] ?: 0),
        TabLabel("Active", counts["active"] ?: 0),
        TabLabel("Completed", counts["completed"] ?: 0)
    )

    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        topBar = {},
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        val startPad = innerPadding.calculateStartPadding(layoutDirection)
        val endPad = innerPadding.calculateEndPadding(layoutDirection)
        val bottomPad = innerPadding.calculateBottomPadding()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = startPad, end = endPad, bottom = bottomPad)
        ) {
            // Header (blue)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 96.dp, max = 140.dp)
                    .background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6))))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.TopStart)) {
                    Text(
                        text = "Order Management",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manage and track all orders",
                        color = Color.White.copy(alpha = 0.95f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search bar
            OrdersSearchBar(
                query = searchQuery,
                onQueryChange = { vm.setSearchQuery(it) },
                modifier = Modifier
                    .padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tabs with badges
            OrderTabsWithBadges(
                selected = selectedTabState,
                labels = labels,
                onTabChange = { idx ->
                    vm.setSelectedTab(idx)
                    vm.setSearchQuery("")
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Body: list / loading / error
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2563EB))
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = error ?: "Unknown error")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(bottom = 84.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredOrders) { order ->
                            val timeText: String = liveTimes[order.id] ?: (order.time_ago ?: computeTimeAgoFromIso(order.created_at))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (order.isBooking) {
                                            vm.selectBooking(order)
                                        } else {
                                            order.id?.toIntOrNull()?.let {
                                                vm.loadOrderDetail(it)
                                            }
                                        }
                                    }
                            ) {
                                OrderListCard(
                                    order = order,
                                    timeText = timeText,
                                    onStatusChange = { orderId, newStatus ->
                                        vm.updateOrderStatusOptimistic(orderId, newStatus) { success, err ->
                                            coroutineScope.launch {
                                                if (success) snackbarHostState.showSnackbar("Status updated to $newStatus")
                                                else snackbarHostState.showSnackbar(err ?: "Failed to update")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Order detail dialog
            if (selectedOrder != null) {
                OrderDetailDialog(
                    order = selectedOrder!!,
                    scrimAlpha = 0.45f,
                    onDismiss = { vm.clearSelectedOrder() },
                    onAccept = { orderId ->
                        val deliveryPartnerId = 4 // This should come from your system

                        vm.updateStatusFromDialog(orderId, "OUT_FOR_DELIVERY", deliveryPartnerId) { success, errorMessage ->
                            coroutineScope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Order accepted and assigned to delivery partner!")
                                } else {
                                    snackbarHostState.showSnackbar(
                                        "Failed to update status: ${errorMessage ?: "Unknown error"}"
                                    )
                                }
                            }

                            if (success) {
                                orderId.toIntOrNull()?.let { id ->
                                    vm.loadOrderDetail(id)
                                }
                            }
                        }

                        vm.clearSelectedOrder() // Close dialog
                    },
                    onReject = { orderId ->
                        // Show reject reason dialog instead of immediately rejecting
                        pendingOrderIdForRejection = orderId
                        showRejectDialog = true
                        // ✅ DO NOT close the main dialog here - let rejectOrderWithReason handle it
                        // vm.clearSelectedOrder() // REMOVE THIS LINE
                    },
                    onMarkDelivered = { orderId ->
                        context.stopService(
                            Intent(
                                context,
                                LocationUpdateService::class.java
                            )
                        )

                        vm.updateStatusFromDialog(orderId, "completed") { success, err ->
                            coroutineScope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Order marked delivered")
                                } else {
                                    snackbarHostState.showSnackbar(err ?: "Failed to update")
                                }
                            }
                            if (success) {
                                orderId.toIntOrNull()?.let { id ->
                                    vm.loadOrderDetail(id)
                                }
                                vm.clearSelectedOrder() // Close dialog
                            }
                        }
                    }
                )
            }
        }
    }
}

/** Tabs with badges using explicit labelled list */
@Composable
fun OrderTabsWithBadges(
    selected: Int,
    labels: List<TabLabel>,
    onTabChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(color = Color(0xFFF1F5F9), shape = MaterialTheme.shapes.large)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEachIndexed { index, tabLabel ->
            val isSelected: Boolean = index == selected
            Surface(
                color = if (isSelected) Color.White else Color.Transparent,
                shape = MaterialTheme.shapes.extraLarge,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .clickable { onTabChange(index) }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = tabLabel.title,
                            color = if (isSelected) Color(0xFF111827) else Color(0xFF6B7280),
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (tabLabel.count > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF1F5F9),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = tabLabel.count.toString(),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}





//package com.example.floatingflavors.app.feature.admin.presentation.orders
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.Place
//import androidx.compose.material.icons.filled.Schedule
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.floatingflavors.app.feature.admin.presentation.order.OrderDetailDialog
//import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrderDto
//import com.example.floatingflavors.app.feature.orders.presentation.OrdersViewModel
//import com.example.floatingflavors.app.feature.order.component.OrderListCard
//import com.example.floatingflavors.app.util.computeTimeAgoFromIso
//import kotlinx.coroutines.isActive
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//
//@Composable
//fun AdminOrdersScreen(vm: OrdersViewModel = viewModel()) {
//    val orders by vm.orders.collectAsState()
//    val isLoading by vm.isLoading.collectAsState()
//    val error by vm.error.collectAsState()
//
//    // selected order detail from VM
//    val selectedOrder by vm.selectedOrder.collectAsState()
//
//    // tabs: 0 = All, 1 = Pending, 2 = Active, 3 = Completed
//    var selectedTab by remember { mutableStateOf(0) }
//
//    // Live time map
//    var liveTimes by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
//
//    // Snackbar host
//    val snackbarHostState = remember { SnackbarHostState() }
//    val coroutineScope = rememberCoroutineScope()
//
//    LaunchedEffect(Unit) { vm.loadOrders() }
//
//    // initialize and refresh liveTimes periodically
//    LaunchedEffect(orders) {
//        liveTimes = orders.associate { order ->
//            (order.id ?: "") to (order.time_ago ?: computeTimeAgoFromIso(order.created_at))
//        }
//    }
//
//    LaunchedEffect(orders) {
//        while (isActive) {
//            liveTimes = orders.associate { order ->
//                (order.id ?: "") to (order.time_ago ?: computeTimeAgoFromIso(order.created_at))
//            }
//            delay(30_000L)
//        }
//    }
//
//    // Filter orders by tab
//    val filteredOrders by remember(orders, selectedTab) {
//        derivedStateOf {
//            when (selectedTab) {
//                1 -> orders.filter { (it.status ?: "").equals("pending", ignoreCase = true) }
//                2 -> orders.filter { (it.status ?: "").equals("active", ignoreCase = true) }
//                3 -> orders.filter { (it.status ?: "").equals("completed", ignoreCase = true) || (it.status ?: "").equals("done", ignoreCase = true) }
//                else -> orders
//            }
//        }
//    }
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
//    ) { padding ->
//        Column(modifier = Modifier
//            .fillMaxSize()
//            .padding(padding)
//        ) {
//
//            // Header: Blue gradient, compact for mobile
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(110.dp)
//                    .background(
//                        brush = Brush.horizontalGradient(
//                            colors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
//                        )
//                    )
//                    .padding(horizontal = 16.dp, vertical = 14.dp)
//            ) {
//                Column(modifier = Modifier.align(Alignment.TopStart)) {
//                    Text("Order Management",
//                        color = Color.White,
//                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
//                    )
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Text("Manage and track all orders",
//                        color = Color.White.copy(alpha = 0.9f),
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//
//            // Tabs row (compact)
//            OrderTabs(selected = selectedTab, onTabChange = { selectedTab = it }, modifier = Modifier.padding(12.dp))
//
//            // List / loading / error
//            when {
//                isLoading -> {
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        CircularProgressIndicator(color = Color(0xFF2563EB))
//                    }
//                }
//                error != null -> {
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text(text = error ?: "Unknown error")
//                    }
//                }
//                else -> {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(horizontal = 12.dp),
//                        contentPadding = PaddingValues(bottom = 84.dp),
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        items(filteredOrders) { order ->
//                            // pass computed live time text to card
//                            val timeText = liveTimes[order.id] ?: (order.time_ago ?: computeTimeAgoFromIso(order.created_at))
//
//                            // Wrap the card in clickable so tapping opens detail dialog
//                            Box(modifier = Modifier
//                                .fillMaxWidth()
//                                .clickable {
//                                    // load detail and VM will expose selectedOrder
//                                    val idInt = order.id?.toIntOrNull()
//                                    if (idInt != null) vm.loadOrderDetail(idInt)
//                                }
//                            ) {
//                                OrderListCard(
//                                    order = order,
//                                    timeText = timeText,
//                                    onStatusChange = { orderId, newStatus ->
//                                        // perform optimistic update and show result snack
//                                        vm.updateOrderStatusOptimistic(orderId, newStatus) { success, err ->
//                                            coroutineScope.launch {
//                                                if (success) {
//                                                    snackbarHostState.showSnackbar("Status updated to $newStatus")
//                                                } else {
//                                                    snackbarHostState.showSnackbar(err ?: "Failed to update status")
//                                                }
//                                            }
//                                        }
//                                    }
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//
//            // Show detail dialog if VM has selectedOrder
//            // Show detail dialog if VM has selectedOrder
//            if (selectedOrder != null) {
//                OrderDetailDialog(
//                    order = selectedOrder!!,
//                    scrimAlpha = 0.45f,
//                    onDismiss = { vm.clearSelectedOrder() },
//                    onAccept = { orderId ->
//                        // Accept from pending -> active
//                        vm.updateStatusFromDialog(orderId, "active") { success, err ->
//                            coroutineScope.launch {
//                                if (success) snackbarHostState.showSnackbar("Order accepted")
//                                else snackbarHostState.showSnackbar(err ?: "Failed to accept")
//                            }
//                            if (success) {
//                                // refresh selected order from server to get latest fields OR update it locally
//                                vm.loadOrderDetail(orderId.toIntOrNull() ?: 0)
//                            }
//                        }
//                    },
//                    onReject = { orderId ->
//                        vm.updateStatusFromDialog(orderId, "rejected") { success, err ->
//                            coroutineScope.launch {
//                                if (success) snackbarHostState.showSnackbar("Order rejected")
//                                else snackbarHostState.showSnackbar(err ?: "Failed to reject")
//                            }
//                            vm.loadOrderDetail(orderId.toIntOrNull() ?: 0)
//                        }
//                    },
//                    onMarkDelivered = { orderId ->
//                        // active -> completed
//                        vm.updateStatusFromDialog(orderId, "completed") { success, err ->
//                            coroutineScope.launch {
//                                if (success) snackbarHostState.showSnackbar("Order marked delivered")
//                                else snackbarHostState.showSnackbar(err ?: "Failed to update")
//                            }
//                            if (success) {
//                                // refresh detail and list
//                                vm.loadOrderDetail(orderId.toIntOrNull() ?: 0)
//                            }
//                        }
//                    }
//                )
//            }
//        }
//    }
//}
//
///** Compact order tab row */
//@Composable
//fun OrderTabs(selected: Int, onTabChange: (Int) -> Unit, modifier: Modifier = Modifier) {
//    val labels = listOf("All", "Pending", "Active", "Completed")
//    Row(
//        modifier = modifier
//            .fillMaxWidth()
//            .height(44.dp)
//            .background(color = Color(0xFFF1F5F9), shape = MaterialTheme.shapes.large)
//            .padding(4.dp),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        labels.forEachIndexed { index, label ->
//            val isSelected = index == selected
//            val bg = if (isSelected) Color.White else Color.Transparent
//            Surface(
//                color = bg,
//                shape = MaterialTheme.shapes.extraLarge,
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxHeight()
//                    .padding(2.dp)
//                    .clickable { onTabChange(index) }
//            ) {
//                Box(contentAlignment = Alignment.Center) {
//                    Text(
//                        text = label,
//                        color = if (isSelected) Color(0xFF111827) else Color(0xFF6B7280),
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//            }
//        }
//    }
//}
//
///** Order detail dialog */
//@Composable
//fun OrderDetailDialog(
//    order: OrderDto,
//    onDismiss: () -> Unit,
//    onAccept: (orderId: String) -> Unit,
//    onReject: (orderId: String) -> Unit
//) {
//    Dialog(onDismissRequest = onDismiss) {
//        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//            // translucent background with slightly darker opacity
//            Box(modifier = Modifier
//                .matchParentSize()
//                .background(Color.Black.copy(alpha = 0.35f))
//                .clickable { onDismiss() } // click outside to dismiss
//            )
//
//            Card(
//                modifier = Modifier
//                    .widthIn(max = 360.dp)
//                    .padding(16.dp),
//                shape = MaterialTheme.shapes.large,
//                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//            ) {
//                Column(modifier = Modifier.padding(18.dp)) {
//                    // Header
//                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
//                        Column {
//                            Text(text = "Order Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
//                            Text(text = order.id ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//                        }
//                        IconButton(onClick = onDismiss) {
//                            Icon(Icons.Default.Close, contentDescription = "Close")
//                        }
//                    }
//
//                    Spacer(Modifier.height(8.dp))
//
//                    // Customer row
//                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
//                        Column {
//                            Text("Customer", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                            Text(order.customer_name ?: "-", style = MaterialTheme.typography.bodyMedium)
//                        }
//                        Column(horizontalAlignment = Alignment.End) {
//                            Text("Phone", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                            Text("+91 98765 43210", style = MaterialTheme.typography.bodyMedium) // replace with real phone if in DB
//                        }
//                    }
//
//                    Spacer(Modifier.height(8.dp))
//
//                    // Address & distance
//                    Text("Address", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                    Text("Sector 15, Noida", style = MaterialTheme.typography.bodyMedium) // replace with DB field if present
//                    Spacer(Modifier.height(6.dp))
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(imageVector = Icons.Default.Place, contentDescription = null)
//                        Spacer(Modifier.width(6.dp))
//                        Text(order.distance ?: "-", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
//                    }
//
//                    Spacer(Modifier.height(12.dp))
//                    Divider()
//                    Spacer(Modifier.height(12.dp))
//
//                    // Items
//                    Text("Order Items:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                    Column(modifier = Modifier.padding(start = 6.dp)) {
//                        order.items?.forEach { it ->
//                            Text("• ${it.name} x${it.qty}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 2.dp))
//                        }
//                    }
//
//                    Spacer(Modifier.height(12.dp))
//
//                    // Total
//                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
//                        Column {
//                            Text("Total Amount", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
//                            Text(order.amount ?: "-", style = MaterialTheme.typography.titleMedium)
//                        }
//                        // Buttons
//                        Row {
//                            Button(
//                                onClick = { onAccept(order.id ?: "") },
//                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
//                            ) {
//                                Text("Accept", color = Color.White)
//                            }
//                            Spacer(modifier = Modifier.width(12.dp))
//                            Button(
//                                onClick = { onReject(order.id ?: "") },
//                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
//                            ) {
//                                Text("Reject", color = Color.White)
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
