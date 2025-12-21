package com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto

@Composable
fun SavedAddressScreen(
    vm: AddressViewModel,
    userId: Int,
    onBack: () -> Unit,
    onAdd: () -> Unit
) {
    LaunchedEffect(Unit) { vm.load(userId) }

    Box(Modifier.fillMaxSize().background(Color(0xFFF6F8F6))) {

        Column {

            // Header (NO TOP BAR)
            Row(
                Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.ArrowBack, null,
                    Modifier.clickable { onBack() })
                Spacer(Modifier.weight(1f))
                Text("Saved Addresses",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(vm.addresses) { addr ->
                    AddressCard(addr,
                        onDelete = { vm.delete(addr.id, userId) }
                    )
                }
            }
        }

        // Add Button
        Button(
            onClick = onAdd,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp)
                .height(56.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF13EC5B)
            )
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Add New Address", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddressCard(
    address: AddressDto,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                address.label,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "${address.house}, ${address.area}\n${address.city} - ${address.pincode}",
                color = Color.Gray,
                fontSize = 13.sp
            )

            if (address.is_default == 1) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "DEFAULT",
                    color = Color(0xFF16A34A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                "Remove",
                color = Color.Red,
                modifier = Modifier.clickable { onDelete() }
            )
        }
    }
}

