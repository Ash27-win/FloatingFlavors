package com.example.floatingflavors.app.feature.user.presentation.membership.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.data.membership.dto.MembershipPlanDto

@Composable
fun MembershipPlanCard(
    plan: MembershipPlanDto,
    isCurrent: Boolean
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        border = if (isCurrent) BorderStroke(2.dp, Color.Magenta) else null
    ) {
        Column(Modifier.padding(16.dp)) {

            Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("${plan.duration_months} months")
            Text("₹${plan.price}", fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))

            Text("✔ ${plan.discount_percent}% discount")
            if (plan.priority_support) Text("✔ Priority support")
            if (plan.free_delivery) Text("✔ Free delivery")
            if (plan.dedicated_manager) Text("✔ Dedicated account manager")

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { /* payment flow */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCurrent) Color.LightGray else Color(0xFFF97316)
                )
            ) {
                Text(if (isCurrent) "Renew Plan" else "Upgrade")
            }
        }
    }
}
