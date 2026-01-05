package com.example.floatingflavors.app.feature.user.presentation.tracking

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.min
import androidx.navigation.NavController


@Composable
fun LiveOrderTrackingScreen(
    navController: NavController,
    orderId: Int,
    orderType: String,
    vm: OrderTrackingViewModel = viewModel()
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Responsive values based on screen size
    val horizontalPadding = when {
        screenWidth < 360.dp -> 12.dp
        screenWidth < 600.dp -> 16.dp
        else -> 20.dp
    }

    val cardPadding = when {
        screenWidth < 360.dp -> 12.dp
        screenWidth < 600.dp -> 16.dp
        else -> 20.dp
    }

    val titleFontSize = when {
        screenWidth < 360.dp -> 20.sp
        screenWidth < 600.dp -> 22.sp
        else -> 24.sp
    }

    val bodyFontSize = when {
        screenWidth < 360.dp -> 12.sp
        screenWidth < 600.dp -> 14.sp
        else -> 16.sp
    }

    val smallFontSize = when {
        screenWidth < 360.dp -> 10.sp
        screenWidth < 600.dp -> 12.sp
        else -> 14.sp
    }

    val state by vm.state.collectAsState()

    LaunchedEffect(orderId) {
        vm.load(orderId, orderType)
    }

    if (state == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val data = state!!
    if (!data.success) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Order not found")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(rememberScrollState())
    ) {
        // ================= HEADER =================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = horizontalPadding, vertical = 16.dp)
        ) {
            Text(
                "Order Tracking",
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Order #${data.orderNumber}",
                fontSize = smallFontSize,
                color = Color.Gray
            )
        }

        // ================= EVENT INFORMATION =================
        ResponsiveCard(
            horizontalPadding = horizontalPadding,
            cardPadding = cardPadding,
            smallFontSize = smallFontSize,
            bodyFontSize = bodyFontSize,
            title = "EVENT INFORMATION",
            content = {
                // Event Type
                ResponsiveRow(
                    label = "Event Type",
                    value = data.eventInfo.title,
                    bodyFontSize = bodyFontSize
                )

                Spacer(Modifier.height(12.dp))
                Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                // Event Date
                ResponsiveRow(
                    label = "Event Date",
                    value = data.eventInfo.date,
                    bodyFontSize = bodyFontSize
                )

                Spacer(Modifier.height(16.dp))
                Divider(color = Color(0xFFF0F0F0), thickness = 1.dp)
                Spacer(Modifier.height(12.dp))

                // Guests
                Text(
                    "Guests",
                    fontSize = bodyFontSize,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${data.eventInfo.people ?: 0} People",
                        fontSize = (bodyFontSize.value + 2.sp.value).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.width(12.dp))
                    // Guest avatars
                    Row {
                        val peopleCount = data.eventInfo.people ?: 0
                        repeat(min(3, peopleCount)) {
                            Box(
                                modifier = Modifier
                                    .size(if (screenWidth < 360.dp) 28.dp else 32.dp)
                                    .background(Color(0xFFE0E0E0), CircleShape)
                                    .padding(2.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "👤",
                                    fontSize = smallFontSize
                                )
                            }
                            Spacer(Modifier.width((-6).dp))
                        }
                        if (peopleCount > 3) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFEFEFEF)
                            ) {
                                Text(
                                    "+${peopleCount - 3}",
                                    Modifier.padding(
                                        horizontal = if (screenWidth < 360.dp) 6.dp else 8.dp,
                                        vertical = if (screenWidth < 360.dp) 3.dp else 4.dp
                                    ),
                                    fontSize = if (screenWidth < 360.dp) 10.sp else 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        )

        // ================= DELIVERY STATUS =================
        ResponsiveCard(
            horizontalPadding = horizontalPadding,
            cardPadding = cardPadding,
            smallFontSize = smallFontSize,
            bodyFontSize = bodyFontSize,
            title = "DELIVERY STATUS",
            content = {
                Spacer(Modifier.height(16.dp))

                // Horizontal Timeline
                val steps = listOf(
                    "CONFIRMED" to Icons.Default.CheckCircle,
                    "PREPARING" to Icons.Default.RestaurantMenu,
                    "OUT_FOR_DELIVERY" to Icons.Default.DeliveryDining,
                    "DELIVERED" to Icons.Default.Home
                )

                val activeIndex = steps.indexOfFirst { it.first == data.currentStatus }

                Column {
                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(Color(0xFFE0E0E0), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(activeIndex / 3f)
                                .height(3.dp)
                                .background(Color(0xFFFF9800), RoundedCornerShape(2.dp))
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Step indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        steps.forEachIndexed { index, (status, icon) ->
                            val completed = index <= activeIndex
                            val active = status == data.currentStatus

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(if (screenWidth < 360.dp) 24.dp else 28.dp)
                                        .background(
                                            if (completed) Color(0xFFFF9800)
                                            else Color(0xFFE0E0E0),
                                            CircleShape
                                        )
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(if (screenWidth < 360.dp) 14.dp else 16.dp)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    status.split("_").joinToString(" "),
                                    fontSize = if (screenWidth < 360.dp) 8.sp else 10.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) Color(0xFFFF9800) else Color.Gray,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Current Status Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0)
                ) {
                    Column(Modifier.padding(if (screenWidth < 360.dp) 12.dp else 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Current Status",
                                    fontSize = smallFontSize,
                                    color = Color(0xFFE65100)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    data.currentStatus.replace("_", " "),
                                    fontSize = bodyFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF9800)
                            ) {
                                Text(
                                    "In Progress",
                                    modifier = Modifier.padding(
                                        horizontal = if (screenWidth < 360.dp) 8.dp else 12.dp,
                                        vertical = if (screenWidth < 360.dp) 4.dp else 6.dp
                                    ),
                                    fontSize = smallFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Status timeline
                        data.statusTimeline.forEach { timeline ->
                            if (timeline.time != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        timeline.status.replace("_", " "),
                                        fontSize = bodyFontSize,
                                        color = Color.Gray
                                    )
                                    Text(
                                        timeline.time ?: "",
                                        fontSize = bodyFontSize,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        )

        // ================= DELIVERY PROGRESS =================
        if (data.currentStatus == "OUT_FOR_DELIVERY" || data.currentStatus == "DELIVERED") {
            ResponsiveCard(
                horizontalPadding = horizontalPadding,
                cardPadding = cardPadding,
                smallFontSize = smallFontSize,
                bodyFontSize = bodyFontSize,
                title = "Delivery In Progress",
                showTitleDecoration = false,
                content = {
                    Spacer(Modifier.height(12.dp))

                    // Delivery Person Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(if (screenWidth < 360.dp) 40.dp else 50.dp)
                                .background(Color(0xFFE0E0E0), CircleShape)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(if (screenWidth < 360.dp) 20.dp else 24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                data.deliveryPerson?.name ?: "Delivery Partner",
                                fontSize = bodyFontSize,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Text(
                                data.deliveryPerson?.vehicle ?: "Bike • XYZ-1234",
                                fontSize = smallFontSize,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Progress Indicator
                    val progress = when (data.currentStatus) {
                        "OUT_FOR_DELIVERY" -> 0.7f
                        "DELIVERED" -> 1.0f
                        else -> 0.3f
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = Color(0xFFFF9800),
                        trackColor = Color(0xFFE0E0E0)
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Estimated Delivery",
                            fontSize = bodyFontSize,
                            color = Color.Gray
                        )
                        Text(
                            if (data.currentStatus == "DELIVERED") "Delivered" else "15-20 min",
                            fontSize = bodyFontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            )
        }

        // ================= MAP =================
        Card(
            modifier = Modifier
                .padding(horizontal = horizontalPadding, vertical = 8.dp)
                .fillMaxWidth()
                .height(if (screenWidth < 360.dp) 180.dp else 200.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            // ================= MAP =================
            Card(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(if (screenWidth < 360.dp) 180.dp else 200.dp),
                shape = RoundedCornerShape(16.dp)
            ) {

                val lat = data.deliveryLocation?.latitude ?: 12.9716
                val lng = data.deliveryLocation?.longitude ?: 77.5946

                Box(modifier = Modifier.fillMaxSize()) {

                    // 🔹 OSM MAP (NO CLICK HERE)
                    OsmMapComposable(
                        latitude = lat,
                        longitude = lng,
                        modifier = Modifier.fillMaxSize()
                    )

                    // 🔹 CLICK OVERLAY (THIS FIXES IT)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Transparent)
                            .clickable {
                                navController.navigate("live_map/$orderId/$orderType")
                            }
                    )

                    // 🔹 OPTIONAL LOCK OVERLAY
                    if (data.currentStatus != "OUT_FOR_DELIVERY") {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Live tracking will start when\norder is out for delivery",
                                    fontSize = smallFontSize,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

            // ================= DELIVERY ADDRESS =================
        ResponsiveCard(
            horizontalPadding = horizontalPadding,
            cardPadding = cardPadding,
            smallFontSize = smallFontSize,
            bodyFontSize = bodyFontSize,
            title = "Delivery Address",
            content = {
                Spacer(Modifier.height(12.dp))
                Text(
                    data.deliveryAddress.line1,
                    fontSize = bodyFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )

                // Split address into multiple lines if too long
                val addressLines = data.deliveryAddress.line1.split(", ")
                if (addressLines.size > 1) {
                    Spacer(Modifier.height(4.dp))
                    addressLines.drop(1).forEach { line ->
                        Text(
                            line,
                            fontSize = smallFontSize,
                            color = Color.Gray
                        )
                    }
                }

                data.deliveryAddress.note?.let {
                    if (it.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    it,
                                    fontSize = smallFontSize,
                                    color = Color.Gray,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        )

        // ================= ACTION BUTTONS =================
        Row(
            modifier = Modifier
                .padding(horizontal = horizontalPadding, vertical = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                modifier = Modifier
                    .weight(1f)
                    .height(if (screenWidth < 360.dp) 48.dp else 56.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                onClick = { /* Contact support */ }
            ) {
                Icon(
                    Icons.Default.HeadsetMic,
                    null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(if (screenWidth < 360.dp) 18.dp else 24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Contact Support",
                    color = Color(0xFFFF9800),
                    fontSize = bodyFontSize
                )
            }

            Button(
                modifier = Modifier
                    .weight(1f)
                    .height(if (screenWidth < 360.dp) 48.dp else 56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                ),
                onClick = { /* Back to home */ }
            ) {
                Icon(
                    Icons.Default.Home,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(if (screenWidth < 360.dp) 18.dp else 24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Back to Home",
                    color = Color.White,
                    fontSize = bodyFontSize
                )
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ResponsiveCard(
    horizontalPadding: Dp,
    cardPadding: Dp,
    smallFontSize: androidx.compose.ui.unit.TextUnit,
    bodyFontSize: androidx.compose.ui.unit.TextUnit,
    title: String,
    showTitleDecoration: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .padding(horizontal = horizontalPadding, vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(cardPadding)) {
            if (showTitleDecoration) {
                Text(
                    title,
                    fontSize = smallFontSize,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            } else {
                Text(
                    title,
                    fontSize = bodyFontSize,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            content()
        }
    }
}

@Composable
private fun ResponsiveRow(
    label: String,
    value: String,
    bodyFontSize: androidx.compose.ui.unit.TextUnit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontSize = bodyFontSize,
            color = Color.Gray
        )
        Text(
            value,
            fontSize = bodyFontSize,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f, fill = false),
            maxLines = 2
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFFFF9800))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 12.sp, color = Color.Gray)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}