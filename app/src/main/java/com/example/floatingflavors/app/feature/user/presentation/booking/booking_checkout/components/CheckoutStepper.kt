package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CheckoutStepper(
    activeStep: Int // 1 = Address, 2 = Summary, 3 = Payment
) {
    val activeColor = CheckoutBlue
    val inactiveColor = Color(0xFFE5E7EB)
    val textInactive = CheckoutTextMuted


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        StepItem(
            number = 1,
            title = "Address",
            isActive = activeStep >= 1,
            activeColor = activeColor,
            inactiveColor = inactiveColor,
            inactiveText = textInactive
        )

        DividerLine()

        StepItem(
            number = 2,
            title = "Summary",
            isActive = activeStep >= 2,
            activeColor = activeColor,
            inactiveColor = inactiveColor,
            inactiveText = textInactive
        )

        DividerLine()

        StepItem(
            number = 3,
            title = "Payment",
            isActive = activeStep >= 3,
            activeColor = activeColor,
            inactiveColor = inactiveColor,
            inactiveText = textInactive
        )
    }
}

@Composable
private fun StepItem(
    number: Int,
    title: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    inactiveText: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (isActive) activeColor else inactiveColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = if (isActive) Color.Black else Color.DarkGray,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) activeColor else inactiveText
        )
    }
}

@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .height(2.dp)
            .width(32.dp)
            .background(Color(0xFFE5E7EB))
    )
}
