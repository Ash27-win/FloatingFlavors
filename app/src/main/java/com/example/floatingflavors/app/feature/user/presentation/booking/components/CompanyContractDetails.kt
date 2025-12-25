package com.example.floatingflavors.app.feature.user.presentation.booking.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompanyContractDetails(
    companyName: String,
    onCompanyNameChange: (String) -> Unit,
    contactPerson: String,
    onContactPersonChange: (String) -> Unit,
    employeeCount: String,
    onEmployeeCountChange: (String) -> Unit,
    contractDuration: String,
    onContractDurationChange: (String) -> Unit,
    serviceFrequency: String,
    onServiceFrequencyChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean = false
) {

    Text("Company Contract", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Text("Corporate catering information", fontSize = 12.sp, color = Color.Gray)

    Spacer(Modifier.height(16.dp))

    Label("Company Name")
    OutlinedTextField(
        value = companyName,
        onValueChange = onCompanyNameChange,
        placeholder = { Text("e.g., ABC Technologies") },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(16.dp))

    Label("Contact Person")
    OutlinedTextField(
        value = contactPerson,
        onValueChange = onContactPersonChange,
        placeholder = { Text("e.g., John Doe") },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(16.dp))

    Label("Employee Count")
    OutlinedTextField(
        value = employeeCount,
        onValueChange = onEmployeeCountChange,
        leadingIcon = { Icon(Icons.Outlined.Groups, null) },
        placeholder = { Text("e.g., 50") },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number
        )
    )

    Spacer(Modifier.height(16.dp))

    Label("Contract Duration")
    OutlinedTextField(
        value = contractDuration,
        onValueChange = onContractDurationChange,
        placeholder = { Text("e.g., 6 Months") },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(16.dp))

    Label("Service Frequency")
    OutlinedTextField(
        value = serviceFrequency,
        onValueChange = onServiceFrequencyChange,
        placeholder = { Text("Daily / Weekly / Monthly") },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(16.dp))

    Label("Notes")
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        minLines = 3,
        placeholder = { Text("Any special requirements...") },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(24.dp))

    PrimaryButton(
        text = if (isLoading) "Submitting..." else "Submit Contract Request",
        onClick = onSubmit,
        enabled = !isLoading
    )
}