package com.example.floatingflavors.app.util

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.ifEmpty { "Select date" },
        onValueChange = {},
        readOnly = true,
        leadingIcon = { Icon(Icons.Outlined.CalendarToday, null) },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )

    if (showDatePicker) {
        // Simple date picker using AlertDialog (works on all Android versions)
        SimpleDatePickerDialog(
            currentDate = value,
            onDateSelected = { selectedDate ->
                onValueChange(selectedDate)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.ifEmpty { "Select time" },
        onValueChange = {},
        readOnly = true,
        leadingIcon = { Icon(Icons.Outlined.Schedule, null) },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )

    if (showTimePicker) {
        // Simple time picker using AlertDialog
        SimpleTimePickerDialog(
            currentTime = value,
            onTimeSelected = { selectedTime ->
                onValueChange(selectedTime)
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun SimpleDatePickerDialog(
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // For simplicity, we'll create a mock date picker
    // In a real app, you might want to use a proper date picker library

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Date") },
        text = { Text("Select a date for your event") },
        confirmButton = {
            TextButton(
                onClick = {
                    // For demo, use current date
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = formatter.format(Date())
                    onDateSelected(date)
                }
            ) {
                Text("Use Today")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SimpleTimePickerDialog(
    currentTime: String,
    onTimeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Time") },
        text = { Text("Select a time for your event") },
        confirmButton = {
            Column {
                // Simple time options
                TextButton(
                    onClick = {
                        onTimeSelected("10:00")
                    }
                ) {
                    Text("10:00 AM")
                }
                TextButton(
                    onClick = {
                        onTimeSelected("12:00")
                    }
                ) {
                    Text("12:00 PM")
                }
                TextButton(
                    onClick = {
                        onTimeSelected("14:00")
                    }
                ) {
                    Text("2:00 PM")
                }
                TextButton(
                    onClick = {
                        onTimeSelected("16:00")
                    }
                ) {
                    Text("4:00 PM")
                }
                TextButton(
                    onClick = {
                        onTimeSelected("18:00")
                    }
                ) {
                    Text("6:00 PM")
                }
                TextButton(
                    onClick = {
                        onTimeSelected("20:00")
                    }
                ) {
                    Text("8:00 PM")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Alternative: Simple text-based date picker (no Material3 dependency)
@Composable
fun SimpleDatePickerText(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDateOptions by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.ifEmpty { "Select date" },
        onValueChange = {},
        readOnly = true,
        leadingIcon = { Icon(Icons.Outlined.CalendarToday, null) },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDateOptions = true },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )

    if (showDateOptions) {
        AlertDialog(
            onDismissRequest = { showDateOptions = false },
            title = { Text("Select Date") },
            text = {
                Column {
                    // Next 7 days
                    val dates = getNext7Days()
                    dates.forEach { date ->
                        OutlinedButton(
                            onClick = {
                                onValueChange(date)
                                showDateOptions = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(date)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDateOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SimpleTimePickerText(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimeOptions by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.ifEmpty { "Select time" },
        onValueChange = {},
        readOnly = true,
        leadingIcon = { Icon(Icons.Outlined.Schedule, null) },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable { showTimeOptions = true },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )

    if (showTimeOptions) {
        AlertDialog(
            onDismissRequest = { showTimeOptions = false },
            title = { Text("Select Time") },
            text = {
                Column {
                    // Common time slots
                    val timeSlots = listOf(
                        "09:00", "10:00", "11:00", "12:00",
                        "13:00", "14:00", "15:00", "16:00",
                        "17:00", "18:00", "19:00", "20:00"
                    )

                    timeSlots.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { time ->
                                OutlinedButton(
                                    onClick = {
                                        onValueChange(time)
                                        showTimeOptions = false
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(time)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showTimeOptions = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Helper functions
fun getNext7Days(): List<String> {
    val dates = mutableListOf<String>()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()

    repeat(7) {
        dates.add(formatter.format(calendar.time))
        calendar.add(Calendar.DAY_OF_YEAR, 1)
    }

    return dates
}

fun getCurrentDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date())
}

fun getCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

// Format display date
fun formatDisplayDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateStr
    }
}

// Format display time
fun formatDisplayTime(timeStr: String): String {
    return try {
        if (timeStr.length >= 5) {
            val hour = timeStr.substring(0, 2).toInt()
            val minute = timeStr.substring(3, 5)

            val period = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour

            "$displayHour:$minute $period"
        } else {
            timeStr
        }
    } catch (e: Exception) {
        timeStr
    }
}