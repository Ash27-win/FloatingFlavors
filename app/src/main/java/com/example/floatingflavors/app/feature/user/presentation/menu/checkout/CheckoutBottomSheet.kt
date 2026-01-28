package com.example.floatingflavors.app.feature.user.presentation.menu.checkout

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floatingflavors.app.feature.user.presentation.menu.cart.CartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    cartVm: CartViewModel,
    onDismiss: () -> Unit,
    onOrderPlaced: () -> Unit,
    vm: CheckoutViewModel = viewModel()
) {
    // 🔥 TEMP USER ID
    val userId = 1

    val state by vm.uiState.collectAsState()
    val payment by vm.payment.collectAsState()
    val addresses by vm.addresses.collectAsState()
    val selectedAddressId by vm.selectedAddressId.collectAsState()

    LaunchedEffect(Unit) {
        vm.load(userId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color(0xFFF6F8F6) // Light background for contrast
    ) {
        Column(Modifier.padding(20.dp).fillMaxHeight(0.9f)) { // Max height for scrolling

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Checkout", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Close, null, Modifier.clickable { onDismiss() })
            }

            Spacer(Modifier.height(16.dp))

            when (state) {
                is CheckoutUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))

                is CheckoutUiState.Error -> Text(
                    (state as CheckoutUiState.Error).msg,
                    color = MaterialTheme.colorScheme.error
                )

                is CheckoutUiState.Success -> {
                    val data = state as CheckoutUiState.Success

                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text("Delivery Address", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        }

                        if (addresses.isEmpty()) {
                            item {
                                Text(
                                    "No addresses found. Please add an address in Settings.",
                                    color = Color.Red,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            items(addresses.size) { index ->
                                val addr = addresses[index]
                                com.example.floatingflavors.app.feature.user.presentation.menu.checkout.components.CheckoutAddressCard(
                                    address = addr,
                                    selected = selectedAddressId == addr.id,
                                    onClick = { vm.setAddress(addr.id) }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }

                        item {
                           HorizontalDivider() 
                        }

                        item {
                            Text("Order Summary", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            data.items.forEach {
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                    Text("${it.name} x${it.qty}", modifier = Modifier.weight(1f))
                                    Text("₹${it.price}", fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth()) {
                                Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(Modifier.weight(1f))
                                Text("₹${data.total}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E63F5))
                            }
                        }

                        item {
                             Spacer(Modifier.height(16.dp))
                             Text("Payment Method", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                             Spacer(Modifier.height(8.dp))
                             PaymentOption("upi", "UPI", payment) { vm.payment.value = "upi" }
                             PaymentOption("card", "Card", payment) { vm.payment.value = "card" }
                             PaymentOption("cod", "Cash on Delivery", payment) { vm.payment.value = "cod" }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { vm.place(userId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E63F5)),
                        enabled = selectedAddressId != null
                    ) {
                        if (selectedAddressId == null) {
                             Text("Select Address First")
                        } else {
                             Text("Place Order - ₹${data.total}", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is CheckoutUiState.Placed -> {
                    LaunchedEffect(Unit) {
                        cartVm.loadCart(userId) // refresh cart
                        onOrderPlaced()
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentOption(
    id: String,
    label: String,
    selected: String,
    onSelect: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected == id, onClick = onSelect)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

