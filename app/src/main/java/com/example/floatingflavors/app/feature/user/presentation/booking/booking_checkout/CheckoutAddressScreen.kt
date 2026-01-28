package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
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
    onContinue: (Int) -> Unit   // ✅ parent expects selected addressId
) {

    /* ---------------- LOAD ADDRESSES ---------------- */
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

        /* ---------------- HEADER ---------------- */
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                modifier = Modifier.clickable { onBack() }
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = "Select Delivery Address",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        CheckoutStepper(activeStep = 1)

        Spacer(Modifier.height(16.dp))

        /* ---------------- ADDRESS LIST ---------------- */
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(vm.addresses) { addr ->
                CheckoutAddressCard(
                    address = addr,
                    selected = vm.selectedAddressId == addr.id,
                    onClick = {
                        // ✅ EXISTING LOGIC – DO NOT CHANGE
                        vm.select(userId, addr.id)
                    }
                )
            }

            /* ---------------- ADD NEW ADDRESS ---------------- */
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
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Address")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        /* ---------------- CONTINUE BUTTON ---------------- */
        Button(
            onClick = {
                // ✅ THIS IS THE KEY LINE
                // Selected addressId parent-ku pass pannrom
                vm.selectedAddressId?.let { selectedId ->
                    onContinue(selectedId)
                }
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
                text = "Proceed to Order Summary →",
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}
