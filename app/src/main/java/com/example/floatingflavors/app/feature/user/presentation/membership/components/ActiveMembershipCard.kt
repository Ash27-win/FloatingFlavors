package com.example.floatingflavors.app.feature.user.presentation.membership.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.data.membership.dto.UserMembershipDto

@Composable
fun ActiveMembershipCard(plan: UserMembershipDto) {
    val displayName = if (plan.name == "Monthly Plan") "Monthly Explorer" else plan.name

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Star/Crown Icon in Orange Circle
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFFFF1E8), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFF6B00),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "ACTIVE PLAN",
                            color = Color(0xFFFF6B00),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = displayName,
                            color = Color(0xFF111111),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Active Badge
                Surface(
                    color = Color(0xFFE8F8F0),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "ACTIVE",
                        color = Color(0xFF24C16C),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Features List
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureRow(text = "${plan.discount_percent}% discount on all orders")
                FeatureRow(text = "Priority booking for events")
                FeatureRow(text = "Free delivery above ₹500")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Thin subtle divider
            HorizontalDivider(
                color = Color(0xFFEEEEEE),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next renewal: ${plan.end_date}",
                    color = Color(0xFF777777),
                    fontSize = 13.sp
                )

                Text(
                    text = "Manage >",
                    color = Color(0xFFFF6B00),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { /* Manage action */ }
                )
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
