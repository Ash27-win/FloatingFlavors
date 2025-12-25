package com.example.floatingflavors.app.feature.admin.presentation.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingDto

@Composable
fun BookingDetailDialog(
    booking: AdminBookingDto,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onAccept) {
                Text("Accept")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onReject) {
                Text("Reject")
            }
        },
        title = {
            Text(
                text = "Booking Details",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                Text("Type: ${booking.booking_type}")
                Text("Status: ${booking.status}")

                if (booking.booking_type == "EVENT") {
                    Text("Event: ${booking.event_name}")
                    Text("People: ${booking.people_count}")
                } else {
                    Text("Company: ${booking.company_name}")
                    Text("Employees: ${booking.employee_count}")
                }

                Text("Created: ${booking.created_at}")
            }
        }
    )
}
