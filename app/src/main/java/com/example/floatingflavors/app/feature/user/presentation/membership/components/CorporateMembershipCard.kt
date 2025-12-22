package com.example.floatingflavors.app.feature.user.presentation.membership.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CorporateMembershipCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0EDFF)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Text("Corporate Memberships", fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(8.dp))
            Text("✔ Customized pricing")
            Text("✔ Dedicated account manager")
            Text("✔ Monthly billing")
            Text("✔ Event planning support")

            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Contact Sales Team")
            }
        }
    }
}
