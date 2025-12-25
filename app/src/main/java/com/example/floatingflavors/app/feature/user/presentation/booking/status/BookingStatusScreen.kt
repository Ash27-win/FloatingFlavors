package com.example.floatingflavors.app.feature.user.presentation.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun BookingStatusScreenWrapper(
    bookingState: BookingViewModel.BookingState.Active,
    userId: Int,
    onNavigateToMenu: (String) -> Unit,
    onBackToBooking: () -> Unit,
    onShowMessage: (String) -> Unit,
    vm: BookingViewModel
) {
    val coroutineScope = rememberCoroutineScope()

    ActiveBookingStatusUIWrapper(
        bookingState = bookingState,
        onCancel = {
            vm.cancelBooking(bookingState.bookingId)
            onBackToBooking()
        },
        onCheckStatus = {
            vm.checkUserBookingStatus(userId)
        },
        onBack = onBackToBooking
    )
}

@Composable
fun ActiveBookingStatusUIWrapper(
    bookingState: BookingViewModel.BookingState.Active,
    onCancel: () -> Unit,
    onCheckStatus: () -> Unit,
    onBack: () -> Unit
) {
    val statusColor = when (bookingState.status) {
        "PENDING" -> Color(0xFFF59E0B) // Orange
        "CONFIRMED" -> Color(0xFF16A34A) // Green
        "CANCELLED" -> Color(0xFFDC2626) // Red
        else -> Color.Gray
    }

    val statusIcon = when (bookingState.status) {
        "PENDING" -> Icons.Filled.HourglassEmpty
        "CONFIRMED" -> Icons.Filled.CheckCircle
        "CANCELLED" -> Icons.Filled.Cancel
        else -> Icons.Filled.Help
    }

    val statusMessage = when (bookingState.status) {
        "PENDING" -> "Your booking is pending admin approval."
        "CONFIRMED" -> "Your booking has been confirmed!"
        "CANCELLED" -> "This booking was cancelled by admin."
        else -> "Unknown status"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = statusIcon,
                contentDescription = "Status",
                tint = statusColor,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = bookingState.status,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )

            Text(
                text = statusMessage,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Booking Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Booking Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Divider()

                // Booking ID
                DetailRowComponentWrapper(label = "Booking ID", value = bookingState.bookingId)

                // Booking Type
                DetailRowComponentWrapper(label = "Type", value = bookingState.bookingType)

                // Event/Company specific details
                if (bookingState.bookingType == "EVENT") {
                    bookingState.eventName?.let {
                        DetailRowComponentWrapper(label = "Event Name", value = it)
                    }
                    DetailRowComponentWrapper(label = "Status", value = bookingState.status)
                    bookingState.dateTime?.let {
                        DetailRowComponentWrapper(label = "Date", value = it)
                    }
                } else {
                    bookingState.companyName?.let {
                        DetailRowComponentWrapper(label = "Company", value = it)
                    }
                    DetailRowComponentWrapper(label = "Status", value = bookingState.status)
                }

                Divider()

                Text(
                    text = "Note: You'll be automatically redirected to menu selection once booking is confirmed.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
        }

        // Actions based on status
        when (bookingState.status) {
            "PENDING" -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        )
                    ) {
                        Icon(Icons.Filled.Cancel, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Booking")
                    }

                    Button(
                        onClick = onCheckStatus,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Check Status")
                    }
                }
            }
            "CONFIRMED" -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Loading indicator for auto-redirect
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = Color.Green
                    )

                    Text(
                        text = "Redirecting to menu selection...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
            "CANCELLED" -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Sorry, this booking cannot be accepted.\nPlease try different timing or contact support.",
                        fontSize = 14.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Replay, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Booking")
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRowComponentWrapper(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}