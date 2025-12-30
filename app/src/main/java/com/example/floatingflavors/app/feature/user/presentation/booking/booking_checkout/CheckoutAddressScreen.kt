package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.components.CheckoutBlue
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.components.CheckoutStepper

@Composable
fun CheckoutAddressScreen(
    vm: CheckoutAddressViewModel,
    userId: Int,
    onBack: () -> Unit,
    onAddAddress: () -> Unit,
    onContinue: (Int) -> Unit
) {
    LaunchedEffect(Unit) {
        vm.load(userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F8F6))
            .padding(horizontal = 20.dp)
    ) {

        Spacer(Modifier.height(16.dp))

        // 🔥 HEADER (THIS WAS MISSING)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ArrowBack,
                null,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.weight(1f))
            Text(
                "Select Delivery Address",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        CheckoutStepper(activeStep = 1)

        Spacer(Modifier.height(16.dp))

        // ADDRESS LIST
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(vm.addresses) { addr ->
                CheckoutAddressCard(
                    address = addr,
                    selected = vm.selectedAddressId == addr.id
                ) {
                    vm.select(userId, addr.id)
                }
            }

            // 🔥 ADD NEW ADDRESS (THIS WAS MISSING)
            item {
                OutlinedButton(
                    onClick = onAddAddress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(30.dp),
                    border = BorderStroke(1.dp, CheckoutBlue),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = CheckoutBlue
                    )
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Address")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // PROCEED BUTTON
        Button(
            onClick = {
                vm.selectedAddressId?.let(onContinue)
            },
            enabled = vm.selectedAddressId != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CheckoutBlue
            )
        ) {
            Text(
                "Proceed to Order Summary →",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

