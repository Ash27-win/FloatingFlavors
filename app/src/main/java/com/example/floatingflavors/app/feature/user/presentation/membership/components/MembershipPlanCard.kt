package com.example.floatingflavors.app.feature.user.presentation.membership.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.data.membership.dto.MembershipPlanDto

@Composable
fun MembershipPlanCard(
    plan: MembershipPlanDto,
    isCurrent: Boolean,
    enabled: Boolean = true,
    onUpgradeClick: () -> Unit
) {
    val duration = plan.duration_months

    // Plan configurations based on duration
    val icon = when (duration) {
        1 -> Icons.Default.FlashOn
        3 -> Icons.Default.DateRange
        else -> Icons.Default.Star
    }

    val iconBgColor = when (duration) {
        1 -> Color(0xFFFFF1E8)
        3 -> Color(0xFFFEF3C7)
        else -> Color(0xFFF3F4F6)
    }

    val iconTintColor = when (duration) {
        1 -> Color(0xFFFF6B00)
        3 -> Color(0xFFD97706)
        else -> Color(0xFF4B5563)
    }

    val badgeText = when {
        isCurrent -> "CURRENT"
        duration == 3 -> "SAVE 15%"
        duration == 6 -> "Popular"
        else -> null
    }

    val badgeBgColor = when {
        isCurrent -> Color(0xFFFFE0CC) // Light orange for current
        duration == 3 -> Color(0xFFE8F8F0) // Light green for savings
        duration == 6 -> Color(0xFFFFF1E8) // Light orange for popular
        else -> Color.Transparent
    }

    val badgeTextColor = when {
        isCurrent -> Color(0xFFFF6B00)
        duration == 3 -> Color(0xFF24C16C)
        duration == 6 -> Color(0xFFFF6B00)
        else -> Color.Transparent
    }

    val subtitle = when (duration) {
        1 -> "30 days of benefits"
        3 -> "Best for regular foodies"
        else -> "Ultimate value & service"
    }

    val priceUnit = when (duration) {
        1 -> "/ month"
        3 -> "/ 3 months"
        else -> "/ 6 months"
    }

    val displayName = when (duration) {
        6 -> "Half-Yearly Elite"
        else -> plan.name
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, if (isCurrent) Color(0xFFFF6B00) else Color(0xFFEEEEEE)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Plan Icon inside circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(iconBgColor, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTintColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = displayName,
                            color = Color(0xFF111111),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subtitle,
                            color = Color(0xFF777777),
                            fontSize = 13.sp
                        )
                    }
                }

                // Top-right floating badge
                if (badgeText != null) {
                    Surface(
                        color = badgeBgColor,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeTextColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Display
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "₹${plan.price.toInt()}",
                    color = Color(0xFF111111),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = priceUnit,
                    color = Color(0xFF777777),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Features List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureRow(text = "${plan.discount_percent}% discount on all orders")
                if (plan.priority_support) {
                    FeatureRow(text = "Standard priority support")
                } else if (duration == 3) {
                    FeatureRow(text = "Priority event access")
                }
                if (plan.dedicated_manager) {
                    FeatureRow(text = "Dedicated support manager")
                }
                if (plan.free_delivery) {
                    FeatureRow(
                        text = if (duration == 6) "Free home delivery ALWAYS" else "Free delivery"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // CTA Button
            when (duration) {
                1 -> {
                    // White outlined button
                    OutlinedButton(
                        onClick = onUpgradeClick,
                        enabled = enabled,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.5.dp, Color(0xFFFF6B00)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF6B00)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Renew Early",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                3 -> {
                    // Orange filled button
                    Button(
                        onClick = onUpgradeClick,
                        enabled = enabled,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B00),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Upgrade Now",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                else -> {
                    // Black premium button
                    Button(
                        onClick = onUpgradeClick,
                        enabled = enabled,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF111111),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Upgrade to Elite",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF24C16C),
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text,
            color = Color(0xFF111111),
            fontSize = 14.sp
        )
    }
}
