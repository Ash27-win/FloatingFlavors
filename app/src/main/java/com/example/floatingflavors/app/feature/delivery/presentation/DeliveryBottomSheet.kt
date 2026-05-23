package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectHorizontalDragGestures

@Composable
fun DeliveryBottomSheet(
    etaMin: Int,
    enabled: Boolean,
    onArrived: () -> Unit
) {
    var showOtpDialog by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    if (showOtpDialog) {
        var otpInput by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = {
                Text(
                    text = "Enter Delivery OTP",
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text("Please enter the 4-digit OTP provided by the customer to confirm delivery completion. (Demo PIN: 1234)")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = otpInput,
                        onValueChange = { if (it.length <= 4) otpInput = it },
                        label = { Text("OTP") },
                        isError = errorMessage,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        )
                    )
                    if (errorMessage) {
                        Text("Invalid OTP. Try 1234.", color = Color.Red, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (otpInput == "1234") {
                            showOtpDialog = false
                            onArrived()
                        } else {
                            errorMessage = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E63F5))
                ) {
                    Text("Verify & Complete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color.White, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: COD Badge & SLA Countdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // COD Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF3E0),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Text(
                        text = "COD • ₹587",
                        color = Color(0xFFE65100),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // SLA Countdown Timer
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFEBEE),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A))
                ) {
                    Text(
                        text = "⏳ Deadline: 45m",
                        color = Color(0xFFC62828),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Estimated Arrival",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$etaMin mins",
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = Color.Black
            )

            Spacer(Modifier.height(24.dp))

            // Communication Action Buttons (Clean & No Text Wrapping)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                            data = android.net.Uri.parse("tel:9876543210")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("📞 Call", color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Button(
                    onClick = {
                        val url = "https://api.whatsapp.com/send?phone=919876543210&text=Hi,%20your%20Floating%20Flavors%20order%20is%20on%20the%20way!"
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("💬 WhatsApp", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            SlideToArriveButton(
                enabled = enabled,
                onArrivedTrigger = { showOtpDialog = true }
            )
        }
    }
}

@Composable
fun SlideToArriveButton(
    enabled: Boolean,
    onArrivedTrigger: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var isConfirmed by remember { mutableStateOf(false) }
    
    val maxDragX = 600f 

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFE5E5E5), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        // Background Text
        Text(
            text = if (isConfirmed) "Delivered / Returning" else "Slide to Deliver",
            color = Color.Gray,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.align(Alignment.Center)
        )
        
        // Draggable Thumb
        if (enabled && !isConfirmed) {
            Box(
                modifier = Modifier
                    .offset { androidx.compose.ui.unit.IntOffset(offsetX.toInt(), 0) }
                    .size(56.dp)
                    .background(Color(0xFF2E63F5), CircleShape)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                if (offsetX > maxDragX * 0.7f) {
                                    isConfirmed = true
                                    onArrivedTrigger()
                                } else {
                                    offsetX = 0f
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount).coerceIn(0f, maxDragX)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ArrowForward, "Slide", tint = Color.White)
            }
        }
    }
}
