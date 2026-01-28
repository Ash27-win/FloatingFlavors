package com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
            .padding(horizontal = 20.dp)
    ) {

        Spacer(Modifier.height(16.dp))

        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ArrowBack,
                null,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.weight(1f))
            Text("Add New Address", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(28.dp))

        Text("ADDRESS TYPE", fontSize = 12.sp, color = Color.Gray)

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            AddressTypeChip("Home", Icons.Default.Home, label == "Home") { label = "Home" }
            AddressTypeChip("Work", Icons.Default.Work, label == "Work") { label = "Work" }
            AddressTypeChip("Other", Icons.Default.Place, label == "Other") { label = "Other" }
        }

        Spacer(Modifier.height(24.dp))

        InputField(
            label = "House No, Building Name",
            hint = "e.g. Flat 4B, Greenwood Heights",
            value = house,
            required = true
        ) { house = it }

        InputField(
            label = "Road name, Area, Colony",
            hint = "e.g. 5th Main Road, Indiranagar",
            value = area,
            required = true
        ) { area = it }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top // 🔥 IMPORTANT
        ) {

            HalfWidthField(modifier = Modifier.weight(1f)) {
                InputField(
                    label = "Pincode",
                    hint = "000000",
                    value = pincode,
                    required = true
                ) { pincode = it }
            }

            HalfWidthField(modifier = Modifier.weight(1f)) {
                InputField(
                    label = "City",
                    hint = "City Name",
                    value = city,
                    required = true
                ) { city = it }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Nearby Landmark", fontWeight = FontWeight.Medium)
            Text("Optional", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(Modifier.height(6.dp))

        InputField(
            "",
            "Famous Shop / Mall / Park",
            landmark,
            leading = Icons.Default.Flag
        ) { landmark = it }

        Spacer(Modifier.weight(1f))

        val context = androidx.compose.ui.platform.LocalContext.current
        Button(
            onClick = {
                vm.add(
                    context = context,
                    userId = userId,
                    label = label,
                    house = house,
                    area = area,
                    pincode = pincode,
                    city = city,
                    landmark = landmark.ifBlank { null }
                ) { onBack() }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13EC5B))
        ) {
            Text("Save Address", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AddressTypeChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFFE6F9EE) else Color.White
    val border = if (selected) Color(0xFF13EC5B) else Color.Transparent

    Column(
        modifier = Modifier
            .size(96.dp, 76.dp)
            .background(bg, RoundedCornerShape(22.dp))
            .border(1.dp, border, RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(top = 12.dp), // 🔥 KEY FIX
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            null,
            tint = Color.Gray,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun InputField(
    label: String,
    hint: String,
    value: String,
    modifier: Modifier = Modifier,
    leading: ImageVector? = null,
    required: Boolean = false,
    onChange: (String) -> Unit
) {
    if (label.isNotEmpty()) {
        Row {
            Text(
                text = label,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
            if (required) {
                Text(
                    text = " *",
                    color = Color.Red,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(6.dp)) // 👈 Figma spacing
    }

    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        placeholder = { Text(hint) },
        leadingIcon = leading?.let { { Icon(it, null) } },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        singleLine = true
    )

    Spacer(Modifier.height(16.dp))
}

@Composable
private fun HalfWidthField(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ) {
        content()
    }
}


