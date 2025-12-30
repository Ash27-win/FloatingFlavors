package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun OrderSuccessScreen(
    transactionId: String,
    paymentMethod: String,
    onBackToHome: () -> Unit,
    onBack: () -> Unit
) {
    var animate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        animate = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // 🔙 Back
        Row(Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null)
            }
        }

        Spacer(Modifier.height(40.dp))

        // ✅ Animated Success Tick
        AnimatedVisibility(
            visible = animate,
            enter = scaleIn(animationSpec = tween(600)) + fadeIn()
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(Color(0x224ADE80), shape = RoundedCornerShape(60.dp))
                )
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .background(Color(0xFF4ADE80), shape = RoundedCornerShape(45.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✓", fontSize = 42.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Payment\nSuccessful",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 30.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Your food order booking has been successfully placed.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(32.dp))

        // 📄 Transaction Card
        Card(
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
        ) {
            Column(Modifier.padding(16.dp)) {

                Text("Transaction Details", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                InfoRow("Transaction ID", transactionId)
                InfoRow(
                    "Transaction Date",
                    SimpleDateFormat("dd - MMM - yy hh:mm a", Locale.getDefault()).format(Date())
                )
                InfoRow("Payment Method", paymentMethod)
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onBackToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E2E))
        ) {
            Text("Back to Home", color = Color.White)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}
