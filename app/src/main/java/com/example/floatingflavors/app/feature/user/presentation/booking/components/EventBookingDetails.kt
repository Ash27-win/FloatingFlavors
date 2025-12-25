package com.example.floatingflavors.app.feature.user.presentation.booking.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventBookingDetails(
    selectedEventType: String,
    onEventTypeChange: (String) -> Unit,
    eventName: String,
    onEventNameChange: (String) -> Unit,
    peopleCount: String,
    onPeopleCountChange: (String) -> Unit,
    eventDate: String,
    onEventDateChange: (String) -> Unit,
    eventTime: String,
    onEventTimeChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean = false
) {

    /* ---------------- DATE STATE ---------------- */
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    /* ---------------- TIME STATE ---------------- */
    var showTimePicker by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState(is24Hour = false)

    /* ---------------- FORMATTERS ---------------- */
    val displayDate = remember(eventDate) {
        if (eventDate.isBlank()) "Select date"
        else {
            try {
                val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = parser.parse(eventDate)
                SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(date!!)
            } catch (_: Exception) {
                eventDate
            }
        }
    }

    val displayTime = remember(eventTime) {
        if (eventTime.isBlank()) "Select time"
        else eventTime
    }

    Column {

        Text("Event Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Tell us about your event", fontSize = 12.sp, color = Color.Gray)

        Spacer(Modifier.height(16.dp))

        Text("Event Type", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        EventTypeGrid(
            selected = selectedEventType,
            onSelect = onEventTypeChange
        )

        Spacer(Modifier.height(20.dp))

        /* ---------------- EVENT NAME ---------------- */
        Label("Event Name")
        OutlinedTextField(
            value = eventName,
            onValueChange = onEventNameChange,
            placeholder = { Text("e.g., Birthday Party") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        /* ---------------- PEOPLE COUNT ---------------- */
        Label("Number of People")
        OutlinedTextField(
            value = peopleCount,
            onValueChange = onPeopleCountChange,
            leadingIcon = { Icon(Icons.Outlined.Group, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        /* ---------------- DATE FIELD ---------------- */
        OutlinedTextField(
            value = displayDate,
            onValueChange = {},
            readOnly = true,
            label = { Text("Event Date") },
            leadingIcon = { Icon(Icons.Outlined.CalendarToday, null) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Outlined.CalendarToday, null)
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        /* ---------------- TIME FIELD ---------------- */
        OutlinedTextField(
            value = displayTime,
            onValueChange = {},
            readOnly = true,
            label = { Text("Event Time") },
            leadingIcon = { Icon(Icons.Outlined.Schedule, null) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Outlined.Schedule, null)
                }
            }
        )

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = if (isLoading) "Submitting..." else "Submit Booking",
            onClick = onSubmit,
            enabled = !isLoading
        )
    }

    /* ---------------- DATE PICKER DIALOG ---------------- */
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        onEventDateChange(sdf.format(Date(millis)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    /* ---------------- TIME PICKER DIALOG ---------------- */
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val h = timeState.hour.toString().padStart(2, '0')
                    val m = timeState.minute.toString().padStart(2, '0')
                    onEventTimeChange("$h:$m")
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Select Time") },
            text = { TimePicker(state = timeState) }
        )
    }
}
