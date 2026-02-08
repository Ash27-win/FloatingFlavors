package com.example.floatingflavors.app.feature.admin.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNotificationClick: () -> Unit = {}
) {
    val counts by viewModel.counts.collectAsState()
    val analytics by viewModel.analytics.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCounts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        Header(unreadCount = unreadCount, onNotificationClick = onNotificationClick)
        Spacer(Modifier.height(12.dp))
        MetricsSection(counts = counts, analytics = analytics)
        Spacer(Modifier.height(12.dp))
        AIInsightsSection()
        Spacer(Modifier.height(12.dp))
        LiveOrderSummarySection(counts = counts)
        Spacer(Modifier.height(12.dp))
        RevenueInsightsSection()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Header(unreadCount: Int = 0, onNotificationClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(92.dp),
        color = Color(0xFFFF6D00),
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Admin Dashboard",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Live Overview",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 12.sp
                )
            }
            
            // Notification Icon
            Box(Modifier.size(40.dp)) {
                IconButton(onClick = onNotificationClick) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                if (unreadCount > 0) {
                     Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color.Red, RoundedCornerShape(50))
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                            .border(1.dp, Color.White, RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricsSection(
    counts: com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersCounts?,
    analytics: com.example.floatingflavors.app.feature.admin.data.remote.AnalyticsDataDto?
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TOTAL ORDERS
            MetricCard(
                bg = Color(0xFF2196F3), 
                value = counts?.total?.toString() ?: "0", 
                title = "Total Orders", 
                caption = "All Time", 
                modifier = Modifier.weight(1f)
            )
            // REVENUE (From Analytics API)
            MetricCard(
                bg = Color(0xFF00C853), 
                value = "₹${analytics?.total_revenue?.toInt() ?: "--"}", 
                title = "Total Revenue", 
                caption = "Revenue", 
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ACTIVE (Cooking/Processing)
            MetricCard(
                bg = Color(0xFF9C27B0), 
                value = counts?.active?.toString() ?: "0", 
                title = "Active / Cooking", 
                caption = "In Kitchen", 
                modifier = Modifier.weight(1f)
            )
            // PENDING (New Orders)
            MetricCard(
                bg = Color(0xFFFF5722), 
                value = counts?.pending?.toString() ?: "0", 
                title = "Pending Orders", 
                caption = "Action Needed", 
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MetricCard(bg: Color, value: String, title: String, caption: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.height(110.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = value, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Column {
                Text(text = title, color = Color.White.copy(alpha = 0.95f), fontSize = 14.sp)
                Text(text = caption, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun AIInsightsSection() {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "AI Insights", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(text = "Smart recommendations for your kitchen", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
            InsightItem("Paneer Biryani demand expected to rise by 18% this weekend.")
            Spacer(Modifier.height(8.dp))
            InsightItem("Peak hour starts in 2 hours. Suggested prep: 30 portions.")
            Spacer(Modifier.height(8.dp))
            InsightItem("Create 'Biryani + Raita Combo' offer. Users frequently order together.")
        }
    }
}

@Composable
private fun InsightItem(text: String) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Box(modifier = Modifier.padding(12.dp)) {
            Text(text = text, fontSize = 13.sp)
        }
    }
}


@Composable
private fun LiveOrderSummarySection(counts: com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersCounts?) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "Live Order Summary", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            // give each small stat a clear background and text color
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // The weight modifier is now correctly applied here, inside the RowScope
                SmallStat(number = counts?.active?.toString() ?: "0", label = "Active", bg = Color(0xFFEEF7FF), contentTint = Color(0xFF2176F3), modifier = Modifier.weight(1f))
                SmallStat(number = counts?.completed?.toString() ?: "0", label = "Completed", bg = Color(0xFFEEFDF3), contentTint = Color(0xFF00A65A), modifier = Modifier.weight(1f))
                SmallStat(number = counts?.pending?.toString() ?: "0", label = "Pending", bg = Color(0xFFFFF6EE), contentTint = Color(0xFFF57C00), modifier = Modifier.weight(1f))
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Determine progress
                val total = (counts?.total ?: 1).toFloat()
                val completed = (counts?.completed ?: 0).toFloat()
                val progress = if(total > 0) completed / total else 0f
                
                LinearProgressIndicator(progress = progress, modifier = Modifier.weight(1f).height(8.dp))
                Spacer(Modifier.width(8.dp))
                Text("${(progress * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun SmallStat(number: String, label: String, bg: Color, contentTint: Color, modifier: Modifier = Modifier) {
    // The incorrect weight modifier is removed from here. The passed-in modifier is used instead.
    Card(modifier = modifier.height(64.dp), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = bg)) {
        // I've added fillMaxSize() and a padding of 8.dp to the Column to ensure content is centered and spaced nicely.
        Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = number, fontWeight = FontWeight.Bold, color = contentTint)
            Text(text = label, fontSize = 12.sp, color = contentTint.copy(alpha = 0.85f))
        }
    }
}

@Composable
private fun RevenueInsightsSection() {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Revenue Insights", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("Weekly performance • +24% vs last week", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))

            val sample = remember { listOf(3000f, 4200f, 4600f, 3800f, 6000f, 7200f, 6800f) }
            val max = sample.maxOrNull() ?: 1f

            Row(modifier = Modifier.fillMaxWidth().height(140.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Bottom) {
                sample.forEach { v ->
                    val fraction = (v / max).coerceIn(0.08f, 1f)
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.BottomCenter) {
                        Box(modifier = Modifier.fillMaxHeight(fraction = fraction).width(18.dp).background(color = Color(0xFFFF6D00), shape = RoundedCornerShape(6.dp)))
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Daily Average\n₹5,457", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("Growth\n+24%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF00C853))
            }
        }
    }
}


/** Dashboard named color tokens (keeps green primary) */
object DashboardColors {
    val orangeHeader = Color(0xFFFF6D00)
    val greenPrimary = Color(0xFF00C853) // your main green
    val purpleCard = Color(0xFF9C27B0)
    val orangeAlert = Color(0xFFFF5722)
}





//package com.example.floatingflavors.app.feature.admin.presentation.dashboard
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun AdminDashboardScreen() {
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF5F5F5))
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(14.dp)
//    ) {
//        item {
//            Text(
//                "Admin Dashboard",
//                style = MaterialTheme.typography.headlineSmall.copy(
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = Color(0xFFFF6A00)
//                )
//            )
//
//            Spacer(Modifier.height(4.dp))
//
//            Text(
//                "Thursday, November 6, 2025",
//                color = Color.Gray,
//                fontSize = 14.sp
//            )
//        }
//
//        /** TOP CARDS **/
//        item {
//            Row(
//                Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                DashboardCard(
//                    bg = Color(0xFF0081FF),
//                    title = "Today's Orders",
//                    value = "42",
//                    label = "Today"
//                )
//
//                DashboardCard(
//                    bg = Color(0xFF00AF4F),
//                    title = "Total Revenue",
//                    value = "₹24.5k",
//                    label = "Today"
//                )
//            }
//        }
//
//        item {
//            Row(
//                Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                DashboardCard(
//                    bg = Color(0xFF8B3EFF),
//                    title = "Active Users",
//                    value = "1,248",
//                    label = "+12%"
//                )
//
//                DashboardCard(
//                    bg = Color(0xFFFF4E00),
//                    title = "Pending Deliveries",
//                    value = "8",
//                    label = "Live"
//                )
//            }
//        }
//
//        /** AI Insights **/
//        item {
//            DashboardSectionCard(
//                title = "AI Insights",
//                subtitle = "Smart recommendations for your kitchen",
//                items = listOf(
//                    "Paneer Biryani demand expected to rise by 18% this weekend.",
//                    "Peak hour starts in 2 hours. Suggested prep: 30 portions.",
//                    "Create 'Biryani + Raita Combo', users frequently order together."
//                )
//            )
//        }
//
//        /** Live Order Summary **/
//        item {
//            LiveOrderSummary()
//        }
//
//        /** Revenue Insights **/
//        item {
//            RevenueInsights()
//        }
//    }
//}
//
//@Composable
//fun DashboardCard(bg: Color, title: String, value: String, label: String) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(120.dp),
//        colors = CardDefaults.cardColors(containerColor = bg),
//        shape = RoundedCornerShape(14.dp)
//    ) {
//        Column(
//            Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(value, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
//            Text(title, color = Color.White)
//            Text(label, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
//        }
//    }
//}
//
//
//@Composable
//fun DashboardSectionCard(title: String, subtitle: String, items: List<String>) {
//    Card(
//        shape = RoundedCornerShape(16.dp),
//        modifier = Modifier.fillMaxWidth()
//    ) {
//        Column(Modifier.padding(16.dp)) {
//            Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
//            Text(subtitle, color = Color.Gray, fontSize = 14.sp)
//
//            Spacer(Modifier.height(12.dp))
//
//            items.forEach { text ->
//                Card(
//                    shape = RoundedCornerShape(12.dp),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 6.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF6EFFF))
//                ) {
//                    Text(
//                        text,
//                        modifier = Modifier.padding(12.dp),
//                        color = Color(0xFF5F00D7)
//                    )
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun LiveOrderSummary() {
//    Card(
//        shape = RoundedCornerShape(16.dp)
//    ) {
//        Column(Modifier.padding(16.dp)) {
//            Text("Live Order Summary", fontWeight = FontWeight.Bold, fontSize = 20.sp)
//            Text("Real-time order tracking", color = Color.Gray, fontSize = 14.sp)
//
//            Spacer(Modifier.height(16.dp))
//
//            Row(
//                Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                SummaryBox("18", "Active")
//                SummaryBox("312", "Completed")
//                SummaryBox("5", "Pending")
//            }
//
//            Spacer(Modifier.height(16.dp))
//
//            Text("Order Processing: 78%")
//            LinearProgressIndicator(
//                progress = 0.78f,
//                modifier = Modifier.fillMaxWidth(),
//                color = Color.Black
//            )
//        }
//    }
//}
//
//@Composable
//fun SummaryBox(value: String, label: String) {
//    Column(horizontalAlignment = Alignment.CenterHorizontally) {
//        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
//        Text(label, color = Color.Gray)
//    }
//}
//
//@Composable
//fun RevenueInsights() {
//    Card(shape = RoundedCornerShape(16.dp)) {
//        Column(Modifier.padding(16.dp)) {
//            Text("Revenue Insights", fontWeight = FontWeight.Bold, fontSize = 20.sp)
//            Text("Weekly performance", color = Color.Gray)
//
//            Spacer(Modifier.height(12.dp))
//
//            BarChartFake()
//
//            Spacer(Modifier.height(14.dp))
//            Text("Daily Average: ₹5,457")
//            Text("Growth: +24%", color = Color(0xFF00AF4F))
//        }
//    }
//}
//
//// Simple Fake Bar Chart
//@Composable
//fun BarChartFake() {
//    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
//        listOf(2800, 3500, 4200, 3800, 5300, 6800, 6100).forEach { height ->
//            Box(
//                modifier = Modifier
//                    .width(22.dp)
//                    .height((height / 10).dp)
//                    .background(Color(0xFFFF6A00), RoundedCornerShape(6.dp))
//            )
//        }
//    }
//}
