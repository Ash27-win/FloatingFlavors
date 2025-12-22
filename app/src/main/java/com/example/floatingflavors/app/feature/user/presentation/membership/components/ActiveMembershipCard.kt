package com.example.floatingflavors.app.feature.user.presentation.membership.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.data.membership.dto.UserMembershipDto

@Composable
fun ActiveMembershipCard(plan: UserMembershipDto) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFFD946EF), Color(0xFFF43F5E))
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🏆 Active Membership", color = Color.White, fontWeight = FontWeight.Bold)
                Text("ACTIVE", color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
            Text(plan.name, color = Color.White, fontSize = 18.sp)

            Spacer(Modifier.height(8.dp))
            Text("✔ ${plan.discount_percent}% discount", color = Color.White)
            Text("✔ Priority booking", color = Color.White)
            Text("✔ Free delivery above ₹500", color = Color.White)

            Spacer(Modifier.height(8.dp))
            Text(
                "Renews on: ${plan.end_date}",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}
