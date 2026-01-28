package com.example.floatingflavors.app.feature.user.presentation.menu.checkout.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto

// Independent colors to avoid dependency issues if not shared
private val CheckoutBlue = Color(0xFF2E63F5)
private val CheckoutBlueSoft = Color(0xFFEAF0FF)
private val CheckoutTextMuted = Color.Gray

@Composable
fun CheckoutAddressCard(
    address: AddressDto,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = CheckoutBlueSoft
        ),
        border = BorderStroke(
            2.dp,
            if (selected) CheckoutBlue else Color.Transparent
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(Modifier.weight(1f)) {
                Text(
                    text = address.label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${address.house}, ${address.area}, ${address.city} - ${address.pincode}",
                    fontSize = 13.sp,
                    color = CheckoutTextMuted,
                    lineHeight = 18.sp
                )
            }

            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = CheckoutBlue,
                    unselectedColor = CheckoutTextMuted
                )
            )
        }
    }
}
