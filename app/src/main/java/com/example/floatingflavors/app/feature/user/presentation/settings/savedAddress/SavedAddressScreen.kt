package com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto

@Composable
fun SavedAddressScreen(
    vm: AddressViewModel,
    userId: Int,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Int) -> Unit
) {
    LaunchedEffect(vm.refreshTrigger) {
        vm.load(userId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F6))
    ) {

        Column {
            Row(
                Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.ArrowBack, null, Modifier.clickable { onBack() })
                Spacer(Modifier.weight(1f))
                Text("Saved Addresses", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(vm.addresses) { addr ->
                    AddressCard(
                        address = addr,
                        onSetDefault = {
                            vm.setDefault(addr.id, userId)
                        },
                        onEdit = { onEdit(addr.id) },
                        onDelete = { vm.delete(addr.id, userId) }
                    )
                }
            }
        }

        Button(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
                .height(56.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF13EC5B))
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add New Address", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AddressCard(
    address: AddressDto,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (icon, tint) = addressIcon(address.label)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    Modifier
                        .size(40.dp)
                        .background(tint.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = tint)
                }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(address.label, fontWeight = FontWeight.Bold)

                    Text(
                        "${address.house}, ${address.area}\n${address.city} - ${address.pincode}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                // ⭐ DEFAULT STAR
                Icon(
                    imageVector =
                        if (address.is_default == 1)
                            Icons.Default.Star
                        else
                            Icons.Default.StarBorder,
                    contentDescription = "Set Default",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSetDefault() }
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                EditIcon(onEdit)
                Spacer(Modifier.width(16.dp))
                Text(
                    "Remove",
                    color = Color.Red,
                    modifier = Modifier.clickable { onDelete() }
                )
            }
        }
    }
}

@Composable
private fun addressIcon(label: String): Pair<ImageVector, Color> {
    return when (label.lowercase()) {
        "home" -> Icons.Default.Home to Color(0xFF16A34A)
        "work" -> Icons.Default.Work to Color(0xFF2563EB)
        "other" -> Icons.Default.Place to Color(0xFF9333EA)
        else -> Icons.Default.LocationOn to Color(0xFF6B7280)
    }
}

@Composable
private fun EditIcon(onClick: () -> Unit) {
    Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Edit",
        tint = Color.Gray,
        modifier = Modifier
            .size(22.dp)
            .clickable { onClick() }
    )
}



