package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import com.example.floatingflavors.app.core.di.PaymentResultBus

@Composable
fun CheckoutPaymentScreen(
    bookingId: Int,
    totalAmount: Double,
    vm: CheckoutPaymentViewModel,
    onBack: () -> Unit,
    onPaymentSuccess: (txnId: String, method: String) -> Unit
) {
    val context = LocalContext.current
    var selectedMethod by remember { mutableStateOf("UPI") }

    // Observe UPI result
    val paymentResult by PaymentResultBus.result.collectAsState()

    LaunchedEffect(paymentResult) {
        when (paymentResult) {
            "SUCCESS" -> {
                vm.markPaymentSuccess(
                    bookingId = bookingId,
                    method = "UPI"
                ) { txnId ->
                    onPaymentSuccess(txnId, "UPI")
                }
            }

            "FAILURE" -> {
                // SHOW MESSAGE, DO NOT NAVIGATE
                Toast.makeText(
                    context,
                    "Payment failed. Please try again or choose COD.",
                    Toast.LENGTH_LONG
                ).show()
            }

            "CANCELLED" -> {
                Toast.makeText(
                    context,
                    "Payment cancelled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

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
            Icon(Icons.Default.ArrowBack, null,
                modifier = Modifier.clickable { onBack() })
            Spacer(Modifier.weight(1f))
            Text("Payments", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
        }

        // TOTAL AMOUNT (matches your figma)
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F0FE))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Total Amount", color = Color.Gray)
                Text(
                    "₹%.2f".format(totalAmount),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB)
                )
            }
        }

        // PAYMENT OPTIONS
        Card(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                PaymentOption(
                    selected = selectedMethod == "UPI",
                    icon = Icons.Default.QrCode,
                    title = "UPI (Google Pay / PhonePe)",
                    onClick = { selectedMethod = "UPI" }
                )
                Divider()
                PaymentOption(
                    selected = selectedMethod == "COD",
                    icon = Icons.Default.Money,
                    title = "Cash on Delivery",
                    onClick = { selectedMethod = "COD" }
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                if (selectedMethod == "UPI") {
                    launchUpiPayment(context, totalAmount)
                } else {
                    vm.markPaymentSuccess(
                        bookingId = bookingId,
                        method = "COD"
                    ) { txnId ->
                        onPaymentSuccess(txnId, "COD")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(56.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text(
                if (selectedMethod == "UPI") "Pay Now" else "Confirm Order",
                fontWeight = FontWeight.Bold
            )
        }
    }
}



/* ---------------- PAYMENT OPTION ROW ---------------- */
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

/* ---------------- UPI INTENT ---------------- */
private fun launchUpiPayment(
    context: android.content.Context,
    amount: Double
) {
    val uri = Uri.parse(
        "upi://pay" +
                "?pa=9566617191@okbizaxis" +
                "&pn=Floating Flavors" +
                "&tn=Booking Payment" +
                "&am=$amount" +
                "&cu=INR"
    )

    val intent = Intent(Intent.ACTION_VIEW, uri)
    (context as Activity).startActivityForResult(intent, 1001)
//    context.startActivity(intent)
}




// NORMAL UI PAYMENT CODE

//package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@Composable
//fun CheckoutPaymentScreen(
//    subtotal: Double,
//    gst: Double,
//    deliveryFee: Double,
//    total: Double,
//    onBack: () -> Unit,
//    onPay: (PaymentMethod) -> Unit
//) {
//
//    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFF6F8F6))
//    ) {
//
//        /* ---------------- HEADER ---------------- */
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                Icons.Default.ArrowBack,
//                contentDescription = null,
//                modifier = Modifier.clickable { onBack() }
//            )
//
//            Spacer(Modifier.width(12.dp))
//
//            Text(
//                "Payments",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(Modifier.weight(1f))
//
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Icon(
//                    Icons.Default.Lock,
//                    contentDescription = null,
//                    tint = Color(0xFF16A34A),
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(Modifier.width(4.dp))
//                Text(
//                    "100% Secure",
//                    color = Color(0xFF16A34A),
//                    fontSize = 12.sp,
//                    fontWeight = FontWeight.Medium
//                )
//            }
//        }
//
//        /* ---------------- AMOUNT CARD ---------------- */
//        Card(
//            modifier = Modifier
//                .padding(16.dp)
//                .fillMaxWidth(),
//            shape = RoundedCornerShape(18.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = Color(0xFFE0EDFF)
//            )
//        ) {
//            Column(Modifier.padding(16.dp)) {
//
//                AmountRow("Subtotal", subtotal)
//                AmountRow("GST (5%)", gst)
//                AmountRow("Delivery fees", deliveryFee)
//
//                Divider(
//                    Modifier.padding(vertical = 12.dp),
//                    color = Color(0xFFB6D4FF)
//                )
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Text(
//                            "Total Amount",
//                            fontWeight = FontWeight.Bold,
//                            color = Color(0xFF2563EB)
//                        )
//                        Spacer(Modifier.width(4.dp))
//                        Icon(
//                            Icons.Default.ExpandLess,
//                            contentDescription = null,
//                            tint = Color(0xFF2563EB)
//                        )
//                    }
//
//                    Text(
//                        "₹%.2f".format(total),
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp,
//                        color = Color(0xFF2563EB)
//                    )
//                }
//            }
//        }
//
//        Spacer(Modifier.height(8.dp))
//
//        /* ---------------- PAYMENT OPTIONS ---------------- */
//
//        PaymentOption(
//            title = "UPI",
//            icon = Icons.Default.AccountBalanceWallet,
//            selected = selectedMethod == PaymentMethod.UPI
//        ) {
//            selectedMethod = PaymentMethod.UPI
//        }
//
//        PaymentOption(
//            title = "Credit / Debit / ATM Card",
//            icon = Icons.Default.CreditCard,
//            selected = selectedMethod == PaymentMethod.CARD
//        ) {
//            selectedMethod = PaymentMethod.CARD
//        }
//
//        Spacer(Modifier.height(8.dp))
//
//        PaymentOption(
//            title = "Cash on Delivery",
//            icon = Icons.Default.Money,
//            selected = selectedMethod == PaymentMethod.COD
//        ) {
//            selectedMethod = PaymentMethod.COD
//        }
//
//        Spacer(Modifier.weight(1f))
//
//        /* ---------------- PAY BUTTON ---------------- */
//        Button(
//            onClick = {
//                selectedMethod?.let { onPay(it) }
//            },
//            enabled = selectedMethod != null,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//                .height(56.dp),
//            shape = RoundedCornerShape(30.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = Color(0xFFFACC15),
//                disabledContainerColor = Color(0xFFE5E7EB)
//            )
//        ) {
//            Text(
//                "Continue",
//                fontWeight = FontWeight.Bold,
//                color = Color.Black
//            )
//        }
//    }
//}
//
///* ---------------- SMALL COMPONENTS ---------------- */
//
//@Composable
//private fun AmountRow(label: String, value: Double) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Text(label)
//        Text("₹%.2f".format(value))
//    }
//}
//
//@Composable
//private fun PaymentOption(
//    title: String,
//    icon: ImageVector,
//    selected: Boolean,
//    onClick: () -> Unit
//) {
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 16.dp)
//            .clickable { onClick() },
//        shape = RoundedCornerShape(14.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        )
//    ) {
//        Row(
//            modifier = Modifier.padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Icon(
//                icon,
//                contentDescription = null,
//                tint = Color.Gray
//            )
//            Spacer(Modifier.width(12.dp))
//            Text(
//                title,
//                fontWeight = FontWeight.SemiBold,
//                modifier = Modifier.weight(1f)
//            )
//            RadioButton(
//                selected = selected,
//                onClick = onClick
//            )
//        }
//    }
//}
//
///* ---------------- PAYMENT TYPE ---------------- */
//
//enum class PaymentMethod {
//    UPI,
//    CARD,
//    COD
//}
