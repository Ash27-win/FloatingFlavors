package com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddAddressScreen(
    vm: AddressViewModel,
    userId: Int,
    onBack: () -> Unit
) {
    var label by remember { mutableStateOf("Home") }
    var customLabel by remember { mutableStateOf("") }

    var house by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var pincode by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var landmark by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F6))
            .padding(20.dp)
    ) {

        /* ---------- HEADER ---------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.weight(1f))
            Text(
                "Add New Address",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        /* ---------- ADDRESS TYPE ---------- */
        AddressTypeSelector(
            selected = label,
            onSelect = {
                label = it
                if (it != "Other") customLabel = ""
            }
        )

        /* ---------- CUSTOM LABEL (ONLY FOR OTHER) ---------- */
        if (label == "Other") {
            AddressInput(
                label = "Address name (e.g. Hostel, Office 2)",
                value = customLabel
            ) { customLabel = it }
        }

        /* ---------- ADDRESS FIELDS ---------- */
        AddressInput("House No, Building*", house) { house = it }
        AddressInput("Road / Area*", area) { area = it }

        Row {
            AddressInput(
                label = "Pincode*",
                value = pincode,
                modifier = Modifier.weight(1f)
            ) { pincode = it }

            Spacer(Modifier.width(12.dp))

            AddressInput(
                label = "City*",
                value = city,
                modifier = Modifier.weight(1f)
            ) { city = it }
        }

        AddressInput("Nearby Landmark (Optional)", landmark) {
            landmark = it
        }

        Spacer(Modifier.weight(1f))

        /* ---------- SAVE BUTTON ---------- */
        Button(
            onClick = {

                val finalLabel =
                    if (label == "Other" && customLabel.isNotBlank())
                        customLabel
                    else
                        label

                vm.add(
                    userId = userId,
                    label = finalLabel,
                    house = house,
                    area = area,
                    pincode = pincode,
                    city = city,
                    landmark = landmark.ifBlank { null }
                ) {
                    onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF13EC5B)
            )
        ) {
            Text("Save Address", fontWeight = FontWeight.Bold)
        }
    }
}

/* ---------- INPUT FIELD ---------- */
@Composable
private fun AddressInput(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onChange: (String) -> Unit
) {
    Text(label, fontSize = 13.sp, color = Color.Gray)
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )
    Spacer(Modifier.height(12.dp))
}

/* ---------- TYPE SELECTOR ---------- */
@Composable
fun AddressTypeSelector(
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AddressTypeChip("Home", selected == "Home") { onSelect("Home") }
        AddressTypeChip("Work", selected == "Work") { onSelect("Work") }
        AddressTypeChip("Other", selected == "Other") { onSelect("Other") }
    }

    Spacer(Modifier.height(24.dp))
}

/* ---------- CHIP ---------- */
@Composable
private fun AddressTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFFE6F9EE) else Color.White
    val border = if (selected) Color(0xFF13EC5B) else Color(0xFFE5E7EB)

    Box(
        modifier = Modifier
            .height(56.dp)
            .background(bg, RoundedCornerShape(20.dp))
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold
        )
    }
}
