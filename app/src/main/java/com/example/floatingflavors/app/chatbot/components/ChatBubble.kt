package com.example.floatingflavors.app.chatbot.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.floatingflavors.app.chatbot.data.ChatEntity
import com.example.floatingflavors.app.chatbot.data.OrderDto
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Composable
fun ChatBubble(
    message: ChatEntity,
    userId: Int,
    onAddToCart: (MenuItemDto) -> Unit,
    onSendMessage: (String) -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            // Group Chat Sender Name
            if (!message.isUser && message.senderName != null) {
                Text(
                    text = message.senderName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100),
                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                )
            }

            // Chat Bubble Body
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (message.isUser) 16.dp else 4.dp,
                    bottomEnd = if (message.isUser) 4.dp else 16.dp
                ),
                color = if (message.isUser) Color(0xFFE65100) else Color(0xFFFFF3E0), // Orange vs Warm Cream
                tonalElevation = 1.dp,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (message.isUser) Color.White else Color(0xFF3E2723), // White vs Dark Brown
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = TimeUtils.formatTime(message.timestamp),
                        fontSize = 9.sp,
                        color = if (message.isUser) Color.White.copy(alpha = 0.7f) else Color.Gray,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            // 🤖 RAG Match Accuracy Badge
            if (!message.isUser) {
                val accuracyVal = ((message.confidence ?: 0.95) * 100).toInt()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (accuracyVal >= 90) Color(0xFF00E676) else Color(0xFFFFB300))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RAG Accuracy: $accuracyVal%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (accuracyVal >= 90) Color(0xFF2E7D32) else Color(0xFFE65100)
                    )
                }
            }

            // Rich Custom Widgets based on Type
            if (!message.isUser) {
                Spacer(modifier = Modifier.height(4.dp))
                when (message.type) {
                    "food_carousel" -> {
                        val items = getMenuItems(message.jsonMetadata)
                        if (items.isNotEmpty()) {
                            FoodCarouselWidget(items = items, onAddToCart = onAddToCart)
                        }
                    }
                    "order_tracking" -> {
                        val orders = getOrders(message.jsonMetadata)
                        if (orders.isNotEmpty()) {
                            OrderTrackingWidget(order = orders.first())
                        }
                    }
                    "support" -> {
                        SupportWidget(
                            onCallClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            },
                            onWhatsAppClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/919876543210"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        )
                    }
                    "booking_progress" -> {
                        BookingProgressWidget()
                    }
                }
            }
        }
    }
}

// 🔹 widget: FOOD CAROUSEL
@Composable
fun FoodCarouselWidget(
    items: List<MenuItemDto>,
    onAddToCart: (MenuItemDto) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            val imageUrl = item.image_full ?: item.image_url
            val isNonVeg = listOf("chicken", "meat", "egg", "fish", "mutton").any {
                (item.name ?: "").lowercase().contains(it)
            }
            val itemIdInt = item.id?.toIntOrNull() ?: 0
            val calories = itemIdInt * 20 + 240
            val rating = 4.0 + (itemIdInt % 10) / 10.0

            Card(
                modifier = Modifier
                    .width(180.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Bestseller Badge
                        if (itemIdInt % 2 == 0) {
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .background(Color(0xFFE65100), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "BESTSELLER",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Veg/Non-Veg Badge
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .border(1.dp, if (isNonVeg) Color.Red else Color(0xFF00B14F))
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (isNonVeg) Color.Red else Color(0xFF00B14F))
                                )
                            }

                            // Rating
                            Text(
                                text = "⭐ ${String.format("%.1f", rating)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF57C00)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = item.name ?: "",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = Color(0xFF3E2723)
                        )

                        Text(
                            text = "🔥 $calories kcal",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "₹${item.price ?: "0"}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFE65100)
                            )

                            Button(
                                onClick = { onAddToCart(item) },
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC80)),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "Add +",
                                    color = Color(0xFFE65100),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// 🔹 widget: ORDER TRACKING TIMELINE
@Composable
fun OrderTrackingWidget(order: OrderDto) {
    val steps = listOf("Placed", "Preparing", "On the Way", "Delivered")
    val currentStep = when (order.status) {
        "PENDING" -> 0
        "PREPARING" -> 1
        "OUT_FOR_DELIVERY" -> 2
        "DELIVERED" -> 3
        else -> 1
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFFFD54F), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Track Order #${order.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFFE65100)
                )
                Text(
                    text = "⏳ Deadline: 45m",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD84315)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Progress Timeline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, stepName ->
                    val isActive = index <= currentStep
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isActive) Color(0xFFE65100) else Color(0xFFE0E0E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (index < currentStep) "✓" else "${index + 1}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stepName,
                            fontSize = 9.sp,
                            fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == currentStep) Color(0xFFE65100) else Color.Gray,
                            maxLines = 1
                        )
                    }
                }
            }

            if (order.status == "OUT_FOR_DELIVERY") {
                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color(0xFFFFD54F).copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFB300)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🚴", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = order.delivery_partner ?: "Assigning Partner...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF3E2723)
                            )
                            Text(
                                text = "Your Delivery Partner",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Text(
                        text = "⭐ 4.9",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFE65100)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { /* Navigate to MapLibre Tracking Screen */ },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📍 Live Track on Map", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 🔹 widget: SUPPORT PANEL
@Composable
fun SupportWidget(
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "💬 Floating Support Desk",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFFE65100)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "How would you prefer to connect with our concierge team?",
                fontSize = 11.sp,
                color = Color(0xFF4E342E)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCallClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("📞 Call Us", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onWhatsAppClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("💬 WhatsApp", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 🔹 widget: BOOKING PROGRESS
@Composable
fun BookingProgressWidget() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, Color(0xFFFFB74D).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFFFFCC80)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📅", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Event Booking Workflow",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF3E2723)
                )
                Text(
                    text = "Complete current step to book your menu.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// JSON HELPERS
private fun getMenuItems(json: String?): List<MenuItemDto> {
    if (json.isNullOrEmpty()) return emptyList()
    return try {
        val type = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = Gson().fromJson(json, type)
        val itemsJson = Gson().toJson(map["menu_items"])
        val itemType = object : TypeToken<List<MenuItemDto>>() {}.type
        Gson().fromJson(itemsJson, itemType) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}

private fun getOrders(json: String?): List<OrderDto> {
    if (json.isNullOrEmpty()) return emptyList()
    return try {
        val type = object : TypeToken<Map<String, Any>>() {}.type
        val map: Map<String, Any> = Gson().fromJson(json, type)
        val itemsJson = Gson().toJson(map["orders"])
        val itemType = object : TypeToken<List<OrderDto>>() {}.type
        Gson().fromJson(itemsJson, itemType) ?: emptyList()
    } catch (e: Exception) {
        emptyList()
    }
}
