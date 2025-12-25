package com.example.floatingflavors.app.feature.user.presentation.booking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingDatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.ifEmpty { "Select date" },
        onValueChange = {},
        readOnly = true,
        leadingIcon = { Icon(Icons.Outlined.CalendarToday, null) },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                showDialog = true
            },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFFE5E7EB)
        )
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Date", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    // Show next 7 days
                    val dates = getNext7Days()

                    dates.forEach { date ->
                        OutlinedButton(
                            onClick = {
                                onValueChange(date)
                                showDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (value == date) Color(0xFFEFF6FF) else Color.Transparent
                            )
                        ) {
                            Text(
                                text = formatDisplayDate(date),
                                color = if (value == date) Color(0xFF2563EB) else Color(0xFF374151)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkingTimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value.ifEmpty { "Select time" },
        onValueChange = {},
        readOnly = true,
        leadingIcon = { Icon(Icons.Outlined.Schedule, null) },
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                showDialog = true
            },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color(0xFFE5E7EB)
        )
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Select Time", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column {
                    // Common time slots
                    val timeSlots = listOf(
                        "09:00", "10:00", "11:00", "12:00",
                        "13:00", "14:00", "15:00", "16:00",
                        "17:00", "18:00", "19:00", "20:00"
                    )

                    // Create 4 columns of 3 rows each
                    timeSlots.chunked(3).forEach { rowTimes ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowTimes.forEach { time ->
                                OutlinedButton(
                                    onClick = {
                                        onValueChange(time)
                                        showDialog = false
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (value == time) Color(0xFFEFF6FF) else Color.Transparent
                                    )
                                ) {
                                    Text(
                                        text = formatDisplayTime(time),
                                        color = if (value == time) Color(0xFF2563EB) else Color(0xFF374151)
                                    )
                                }
                            }
                            // Fill empty slots if row has less than 3 items
                            repeat(3 - rowTimes.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
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

fun formatDisplayDate(dateStr: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE, MMM dd", Locale.getDefault())
        val date = inputFormat.parse(dateStr)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        dateStr
    }
}

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

fun getCurrentDate(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return formatter.format(Date())
}

fun getCurrentTime(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}