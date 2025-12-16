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

    LaunchedEffect(Unit) {
        vm.load(userId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(Modifier.padding(20.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Checkout", fontSize = 20.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Default.Close, null, Modifier.clickable { onDismiss() })
            }

            Spacer(Modifier.height(16.dp))

            when (state) {
                is CheckoutUiState.Loading -> CircularProgressIndicator()

                is CheckoutUiState.Error -> Text(
                    (state as CheckoutUiState.Error).msg,
                    color = MaterialTheme.colorScheme.error
                )

                is CheckoutUiState.Success -> {
                    val data = state as CheckoutUiState.Success

                    data.items.forEach {
                        Text("${it.name} x${it.qty} - ₹${it.price}")
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Total: ₹${data.total}")

                    Spacer(Modifier.height(16.dp))

                    PaymentOption("upi", "UPI", payment) { vm.payment.value = "upi" }
                    PaymentOption("card", "Card", payment) { vm.payment.value = "card" }
                    PaymentOption("cod", "Cash on Delivery", payment) { vm.payment.value = "cod" }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { vm.place(userId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Place Order")
                    }
                }

                is CheckoutUiState.Placed -> {
                    LaunchedEffect(Unit) {
                        cartVm.loadCart(userId) // refresh cart
                        onOrderPlaced()
                    }
                }


//                is CheckoutUiState.Placed -> {
//                    LaunchedEffect(Unit) { onOrderPlaced() }
//                }
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

