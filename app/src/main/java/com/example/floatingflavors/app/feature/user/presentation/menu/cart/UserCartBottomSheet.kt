package com.example.floatingflavors.app.feature.user.presentation.menu.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ---------------- UI MODEL ---------------- */

data class CartItemUi(
    val cartItemId: Int,
    val name: String,
    val price: Int,
    val quantity: Int
)

/* ---------------- BOTTOM SHEET ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCartBottomSheet(
    items: List<CartItemUi>,
    totalAmount: Int,
    onDismiss: () -> Unit,
    onIncrease: (CartItemUi) -> Unit,
    onDecrease: (CartItemUi) -> Unit,
    onRemove: (CartItemUi) -> Unit,
    onCheckout: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            /* -------- HEADER -------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Your Cart", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${items.size} items in your cart",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDismiss() }
                )
            }

            Spacer(Modifier.height(16.dp))

            /* -------- CART ITEMS -------- */
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = onIncrease,
                        onDecrease = onDecrease,
                        onRemove = onRemove
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(16.dp))

            /* -------- TOTAL -------- */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Amount", fontSize = 16.sp, color = Color.Gray)
                Text(
                    "₹$totalAmount",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00A63E)
                )
            }

            Spacer(Modifier.height(20.dp))

            /* -------- CHECKOUT -------- */
            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00A63E)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Proceed to Checkout →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

/* ---------------- CART ITEM ROW ---------------- */

@Composable
private fun CartItemRow(
    item: CartItemUi,
    onIncrease: (CartItemUi) -> Unit,
    onDecrease: (CartItemUi) -> Unit,
    onRemove: (CartItemUi) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF6F6F6), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(
                "₹${item.price}",
                color = Color(0xFF00A63E),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                QuantityButton(Icons.Default.Remove) { onDecrease(item) }

                Text(
                    item.quantity.toString(),
                    modifier = Modifier.width(28.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )

                QuantityButton(Icons.Default.Add) { onIncrease(item) }
            }
        }

        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove",
            tint = Color.Gray,
            modifier = Modifier
                .size(20.dp)
                .clickable { onRemove(item) }
        )
    }
}

/* ---------------- QUANTITY BUTTON ---------------- */

@Composable
private fun QuantityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(Color.White, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.Gray)
    }
}
