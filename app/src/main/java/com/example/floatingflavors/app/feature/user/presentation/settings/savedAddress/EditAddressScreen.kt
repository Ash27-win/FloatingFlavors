package com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto
import com.example.floatingflavors.app.feature.user.data.settings.dto.EditAddressRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAddressScreen(
    vm: EditAddressViewModel,
    address: AddressDto,
    userId: Int,
    onBack: () -> Unit
) {
    /* ---------- STATE ---------- */
    var label by remember { mutableStateOf(address.label) }
    var house by remember { mutableStateOf(address.house) }
    var area by remember { mutableStateOf(address.area) }
    var pincode by remember { mutableStateOf(address.pincode) }
    var city by remember { mutableStateOf(address.city) }
    var landmark by remember { mutableStateOf(address.landmark ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(vm.snackbarMessage) {
        vm.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSnackbar()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            errorMessage = null
        }
    }

    /* ---------- ROOT ---------- */
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },

        /* ---------- FIXED HEADER ---------- */
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Edit Address",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },

        /* ---------- FIXED BOTTOM BUTTON ---------- */
        bottomBar = {
            Button(
                onClick = {
                    when {
                        house.isBlank() -> errorMessage = "House is required"
                        area.isBlank() -> errorMessage = "Area is required"
                        pincode.length != 6 || !pincode.all { it.isDigit() } ->
                            errorMessage = "Enter valid 6-digit pincode"
                        city.isBlank() -> errorMessage = "City is required"
                        else -> {
                            vm.updateAddress(
                                EditAddressRequest(
                                    address_id = address.id,
                                    user_id = userId,
                                    label = label,
                                    house = house,
                                    area = area,
                                    pincode = pincode,
                                    city = city,
                                    landmark = landmark.ifBlank { null },
                                    is_default = address.is_default
                                ),
                                onSuccess = onBack
                            )
                        }
                    }
                },
                modifier = Modifier
                    .padding(16.dp)
                    .height(56.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF13EC5B)
                )
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, null)
            }
        }
    ) { padding ->

        /* ---------- SCROLLABLE CONTENT ---------- */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {

            /* ---------- MAP ---------- */
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Map Preview (Temporary)", color = Color.DarkGray)
            }

            /* ---------- LABEL AS ---------- */
            Text(
                "LABEL AS",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 20.dp, top = 8.dp)
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(Modifier.weight(1f)) {
                    AddressTypeChip("Home", Icons.Default.Home, label == "Home") {
                        label = "Home"
                    }
                }
                Box(Modifier.weight(1f)) {
                    AddressTypeChip("Work", Icons.Default.Work, label == "Work") {
                        label = "Work"
                    }
                }
                Box(Modifier.weight(1f)) {
                    AddressTypeChip("Other", Icons.Default.Place, label == "Other") {
                        label = "Other"
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            /* ---------- FORM ---------- */
            FormField("House No, Building Name", true, house) { house = it }
            FormField("Road name, Area, Colony", true, area) { area = it }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormField("Pincode", true, pincode, Modifier.weight(1f)) { pincode = it }
                FormField("City", true, city, Modifier.weight(1f)) { city = it }
            }

            FormField(
                label = "Nearby Landmark",
                required = false,
                value = landmark,
                hint = "e.g. Near Metro Station",
                icon = Icons.Default.Flag
            ) { landmark = it }

            Spacer(Modifier.height(80.dp)) // space above button
        }
    }
}

/* ---------- ADDRESS TYPE CHIP ---------- */
@Composable
private fun AddressTypeChip(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) Color(0xFF13EC5B) else Color.White
    val txt = if (selected) Color.Black else Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(bg, RoundedCornerShape(30.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(30.dp))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, null, tint = txt)
        Spacer(Modifier.width(6.dp))
        Text(text, color = txt, fontWeight = FontWeight.SemiBold)
    }
}

/* ---------- FORM FIELD ---------- */
@Composable
private fun FormField(
    label: String,
    required: Boolean,
    value: String,
    modifier: Modifier = Modifier,
    hint: String = "",
    icon: ImageVector? = null,
    onChange: (String) -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp).then(modifier)) {
        Row {
            Text(label, fontWeight = FontWeight.SemiBold)
            if (required) Text(" *", color = Color.Red)
        }
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            placeholder = { Text(hint) },
            leadingIcon = icon?.let { { Icon(it, null) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
    }
}
