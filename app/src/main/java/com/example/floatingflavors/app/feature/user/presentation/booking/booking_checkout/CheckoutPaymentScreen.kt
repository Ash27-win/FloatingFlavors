package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.user.presentation.membership.PaymentGatewayManager
import kotlinx.coroutines.launch

@Composable
fun CheckoutPaymentScreen(
    bookingId: Int,
    totalAmount: Double,
    vm: CheckoutPaymentViewModel,
    onBack: () -> Unit,
    onPaymentSuccess: (txnId: String, method: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userId = com.example.floatingflavors.app.core.UserSession.userId

    var selectedMethod by remember { mutableStateOf("UPI") }
    var hasActiveMembership by remember { mutableStateOf(false) }
    var suggestedPlanId by remember { mutableStateOf<Int?>(null) }
    var suggestedPlanPrice by remember { mutableStateOf(0.0) }
    var suggestedPlanCode by remember { mutableStateOf("") }
    var includeMembership by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    // Load active membership to check for cross-selling recommendations
    LaunchedEffect(Unit) {
        try {
            val response = NetworkClient.membershipApi.getMembership(userId)
            if (response.currentPlan == null) {
                hasActiveMembership = false
                // Recommending Elite Plan for corporate/event bookings
                val elitePlan = response.availablePlans.find { it.id == 3 }
                if (elitePlan != null) {
                    suggestedPlanId = elitePlan.id
                    suggestedPlanPrice = elitePlan.price
                    suggestedPlanCode = "ELITE"
                }
            } else {
                hasActiveMembership = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val membershipPrice = if (includeMembership) suggestedPlanPrice else 0.0
    val finalAmount = totalAmount + membershipPrice
    val referenceId = "TXN_BOOK_${bookingId}_" + System.currentTimeMillis()

    // ActivityResultLauncher for clean UPI payments
    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isProcessing = true
        if (result.resultCode == Activity.RESULT_OK) {
            val response = result.data?.getStringExtra("response") ?: ""
            if (response.contains("SUCCESS", true)) {
                coroutineScope.launch {
                    try {
                        if (includeMembership && suggestedPlanId != null) {
                            // 1. Create a pending transaction on backend for the membership
                            val subResponse = NetworkClient.membershipApi.subscribeMembership(userId, suggestedPlanId!!)
                            if (subResponse.success && subResponse.reference_id != null) {
                                // 2. Verify the payment for the membership
                                NetworkClient.membershipApi.verifyPayment(subResponse.reference_id, "SUCCESS")
                            }
                        }
                        
                        // 3. Mark the booking as paid
                        vm.markPaymentSuccess(bookingId, "UPI") { txnId ->
                            isProcessing = false
                            onPaymentSuccess(txnId, "UPI")
                        }
                    } catch (e: Exception) {
                        isProcessing = false
                        e.printStackTrace()
                        Toast.makeText(context, "Error verifying transaction: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                isProcessing = false
                Toast.makeText(context, "Payment failed: $response", Toast.LENGTH_LONG).show()
            }
        } else {
            isProcessing = false
            Toast.makeText(context, "Payment cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F8F6))
        ) {
            // HEADER
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ArrowBack, null,
                    modifier = Modifier.clickable(enabled = !isProcessing) { onBack() }
                )
                Spacer(Modifier.weight(1f))
                Text("Payments", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
            }

            // TOTAL AMOUNT
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Total Amount", color = Color.Gray)
                    Text(
                        "₹%.2f".format(finalAmount),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2563EB)
                    )
                }
            }

            // MEMBERSHIP BUNDLING RECOMMENDATION CARD
            if (!hasActiveMembership && suggestedPlanId != null) {
                Card(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1E8)),
                    border = BorderStroke(1.dp, Color(0xFFFF6B00))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Checkbox(
                            checked = includeMembership,
                            onCheckedChange = { includeMembership = it },
                            enabled = !isProcessing,
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF6B00))
                        )
                        Column {
                            Text(
                                text = "Bundle Half-Yearly Elite!",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B00),
                                fontSize = 14.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Add for ₹${suggestedPlanPrice.toInt()} and get 20% discount on this booking & all future orders.",
                                color = Color(0xFF111111),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // PAYMENT OPTIONS
            Card(
                modifier = Modifier.padding(16.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    PaymentOption(
                        selected = selectedMethod == "UPI",
                        icon = Icons.Default.QrCode,
                        title = "UPI (Google Pay / PhonePe)",
                        onClick = { if (!isProcessing) selectedMethod = "UPI" }
                    )
                    HorizontalDivider()
                    PaymentOption(
                        selected = selectedMethod == "COD",
                        icon = Icons.Default.Money,
                        title = "Cash on Delivery",
                        onClick = { if (!isProcessing) selectedMethod = "COD" }
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    if (selectedMethod == "UPI") {
                        isProcessing = true
                        val intent = PaymentGatewayManager.buildUpiIntent(
                            amount = finalAmount,
                            referenceId = referenceId,
                            planCode = if (includeMembership) suggestedPlanCode else "BOOKING"
                        )
                        upiLauncher.launch(intent)
                    } else {
                        isProcessing = true
                        coroutineScope.launch {
                            try {
                                if (includeMembership && suggestedPlanId != null) {
                                    val subResponse = NetworkClient.membershipApi.subscribeMembership(userId, suggestedPlanId!!)
                                    if (subResponse.success && subResponse.reference_id != null) {
                                        NetworkClient.membershipApi.verifyPayment(subResponse.reference_id, "SUCCESS")
                                    }
                                }
                                vm.markPaymentSuccess(bookingId, "COD") { txnId ->
                                    isProcessing = false
                                    onPaymentSuccess(txnId, "COD")
                                }
                            } catch (e: Exception) {
                                isProcessing = false
                                e.printStackTrace()
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text(
                    if (isProcessing) "Processing..." else if (selectedMethod == "UPI") "Pay Now" else "Confirm Order",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Loading indicator
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFFFF6B00))
            }
        }
    }
}

@Composable
private fun PaymentOption(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(10.dp))
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(title, fontWeight = FontWeight.Medium)
    }
}
