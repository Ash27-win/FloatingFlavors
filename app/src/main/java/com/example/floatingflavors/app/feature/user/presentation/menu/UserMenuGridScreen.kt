package com.example.floatingflavors.app.feature.user.presentation.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel
import com.example.floatingflavors.app.feature.user.data.cart.dto.CartItemDto
import com.example.floatingflavors.app.feature.user.presentation.menu.cart.*
import com.example.floatingflavors.app.feature.user.presentation.menu.checkout.CheckoutBottomSheet
import com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout.OrderSuccessScreen

@Composable
fun UserMenuGridScreen(
    menuVm: MenuViewModel = viewModel(),
    cartVm: CartViewModel = viewModel(),
    onItemClick: (MenuItemDto) -> Unit = {}
) {
    // 🔥 TEMP USER ID (SessionManager removed ONLY)
    val userId = 1

    val cartState by cartVm.uiState.collectAsState()
    val cartItems: List<CartItemDto> =
        (cartState as? CartUiState.Success)?.items ?: emptyList<CartItemDto>()
    val cartBadgeCount = cartItems.sumOf { it.quantity }

    var showCart by remember { mutableStateOf(false) }
    var showCheckout by remember { mutableStateOf(false) }
    var showOrderSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        menuVm.loadMenu()
        cartVm.loadCart(userId)
    }

    Box(Modifier.fillMaxSize()) {

        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFEBEBEB))
        ) {

            /* ---------- TOP BAR ---------- */
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Menu", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Browse our delicious offerings", fontSize = 13.sp, color = Color.Gray)
                }

                Box(Modifier.clickable { showCart = true }) {
                    Icon(Icons.Default.ShoppingCart, null, Modifier.size(28.dp))

                    if (cartBadgeCount > 0) {
                        Box(
                            Modifier
                                .size(18.dp)
                                .align(Alignment.TopEnd)
                                .offset(6.dp, (-6).dp)
                                .background(Color(0xFF00C853), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                cartBadgeCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            /* ---------- SEARCH ---------- */
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, null, tint = Color.Gray, modifier = Modifier.padding(12.dp))
                Text("Search for dishes...", color = Color.Gray)
            }

            Spacer(Modifier.height(12.dp))

            /* ---------- GRID ---------- */
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(menuVm.menuItems) { menuItem ->

                    // 🔑 FINAL FIX: convert String? → Int ONCE
                    val menuId = menuItem.id?.toIntOrNull() ?: return@items

                    val cartItem = cartItems.firstOrNull { cart ->
                        cart.menuItemId == menuId
                    }

                    val quantity = cartItem?.quantity ?: 0

                    MenuGridCard(
                        item = menuItem,
                        quantity = quantity,
                        onAdd = {
                            cartVm.add(
                                userId = userId,
                                menuId = menuId,          // ✅ Int
                                price = menuItem.price
                                    ?.toString()
                                    ?.toDoubleOrNull()
                                    ?.toInt()
                                    ?: 0
                            )
                        },
                        onIncrease = {
                            cartItem?.let {
                                cartVm.increase(userId, it.cartItemId)
                            }
                        },
                        onDecrease = {
                            cartItem?.let {
                                if (it.quantity > 1)
                                    cartVm.decrease(userId, it.cartItemId)
                                else
                                    cartVm.remove(userId, it.cartItemId)
                            }
                        },
                        onClick = { onItemClick(menuItem) }
                    )
                }
            }
        }

        /* ---------- CART ---------- */
        /* ---------- CART ---------- */
        if (showCart && cartItems.isNotEmpty()) {

            val state = cartState as CartUiState.Success

            UserCartBottomSheet(
                items = state.items.map { item ->
                    CartItemUi(
                        cartItemId = item.cartItemId,
                        name = item.name,
                        price = item.price,
                        quantity = item.quantity
                    )
                },
                totalAmount = state.total,
                onDismiss = { showCart = false },
                onIncrease = { cartVm.increase(userId, it.cartItemId) },
                onDecrease = {
                    if (it.quantity > 1)
                        cartVm.decrease(userId, it.cartItemId)
                    else
                        cartVm.remove(userId, it.cartItemId)
                },
                onRemove = { cartVm.remove(userId, it.cartItemId) },
                onCheckout = {
                    showCart = false
                    showCheckout = true
                }
            )
        }

        if (showCheckout) {
            CheckoutBottomSheet(
                cartVm = cartVm,
                onDismiss = { showCheckout = false },
                onOrderPlaced = {
                    showCheckout = false
                    showOrderSuccess = true
                }
            )
        }

        if (showOrderSuccess) {
            AlertDialog(
                onDismissRequest = { showOrderSuccess = false },
                confirmButton = {
                    TextButton(onClick = { showOrderSuccess = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Order Placed") },
                text = { Text("Your order has been placed successfully!") }
            )
        }
    }
}

/* ---------------- MENU GRID CARD (UNCHANGED UI) ---------------- */

@Composable
private fun MenuGridCard(
    item: MenuItemDto,
    quantity: Int,
    onAdd: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onClick: () -> Unit
) {
    // ✅ CORRECT image field
    val fullImageUrl: String? =
        when {
            !item.image_full.isNullOrBlank() ->
                item.image_full

            else -> null
        }

    Column(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(10.dp)
    ) {

        // ✅ MENU GRID IMAGE (FIGMA SAFE)
        AsyncImage(
            model = fullImageUrl,
            contentDescription = item.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE0E0E0)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.height(8.dp))

        Text(item.name ?: "-", fontWeight = FontWeight.Bold)
        Text(item.category ?: "", fontSize = 12.sp, color = Color.Gray)

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

            Text(
                "₹${item.price ?: 0}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00A86B)
            )

            Spacer(Modifier.weight(1f))

            if (quantity == 0) {
                Box(
                    Modifier
                        .height(32.dp)
                        .width(70.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF00C853))
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+ Add", color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF00C853))
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "−",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.clickable { onDecrease() }
                    )
                    Text(
                        " $quantity ",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "+",
                        color = Color.White,
                        fontSize = 20.sp,
                        modifier = Modifier.clickable { onIncrease() }
                    )
                }
            }
        }
    }
}













//import android.util.Log
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.platform.LocalContext
//import androidx.lifecycle.viewmodel.compose.viewModel
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel
//import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
//import androidx.compose.ui.text.style.TextAlign
//
//@Composable
//fun UserMenuScreen(
//    vm: MenuViewModel = viewModel()
//) {
//    // read ViewModel state directly (Compose observes these)
//    val isLoading = vm.isLoading
//    val error = vm.errorMessage
//    val items = vm.menuItems
//
//    // load menu once
//    LaunchedEffect(Unit) {
//        vm.loadMenu()
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(12.dp)
//    ) {
//        Text("Menu", style = MaterialTheme.typography.titleLarge)
//        Spacer(Modifier.height(8.dp))
//
//        when {
//            isLoading -> {
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator()
//                }
//            }
//
//            !error.isNullOrEmpty() -> {
//                Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
//            }
//
//            items.isEmpty() -> {
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    Text("No items yet")
//                }
//            }
//
//            else -> {
//                LazyColumn(modifier = Modifier.fillMaxSize()) {
//                    items(items) { item ->
//                        MenuRow(item = item)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun MenuRow(item: MenuItemDto) {
//    val ctx = LocalContext.current
//    val url = item.image_url ?: ""
//
//    LaunchedEffect(item.id) {
//        Log.d("UserMenu", "LOAD_IMAGE -> id=${item.id} url=$url")
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 6.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp)
//        ) {
//            if (url.isNotBlank()) {
//                val request = ImageRequest.Builder(ctx)
//                    .data(url)
//                    .crossfade(true)
//                    .listener(
//                        onSuccess = { _request, _metadata ->
//                            Log.d("Coil", "Loaded image: $url")
//                        },
//                        onError = { _request, throwable ->
//                            val type = throwable?.javaClass?.simpleName ?: "UnknownError"
//                            val msg = throwable?.toString() ?: "no message"
//                            Log.e("Coil", "Failed load: $url -> $type: $msg")
//                        }
//                    )
//                    .build()
//
//                AsyncImage(
//                    model = request,
//                    contentDescription = item.name,
//                    modifier = Modifier.size(80.dp)
//                )
//            } else {
//                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
//                    Text(
//                        "No\nImage",
//                        style = MaterialTheme.typography.bodySmall,
//                        textAlign = TextAlign.Center
//                    )
//                }
//            }
//
//            Spacer(Modifier.width(12.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(item.name, style = MaterialTheme.typography.titleMedium)
//                item.description?.let {
//                    Spacer(Modifier.height(4.dp))
//                    Text(it, style = MaterialTheme.typography.bodySmall)
//                }
//                Spacer(Modifier.height(6.dp))
//                Text("₹ ${item.price}", style = MaterialTheme.typography.bodyMedium)
//            }
//        }
//    }
//}
