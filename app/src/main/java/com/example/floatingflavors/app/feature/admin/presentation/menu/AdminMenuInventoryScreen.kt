package com.example.floatingflavors.app.feature.admin.presentation.menu

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.core.navigation.Screen
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel

/**
 * Admin Menu & Inventory - Final Figma Design Implementation (Tabs not scrollable)
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuInventoryScreen(
    navController: NavController,
    vm: MenuViewModel = viewModel()
) {
    val items by remember { derivedStateOf { vm.menuItems } }
    val isLoading by remember { derivedStateOf { vm.isLoading } }
    val error by remember { derivedStateOf { vm.errorMessage } }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All", "Main", "Desserts", "Drinks")

    // load menu once
    LaunchedEffect(Unit) { vm.loadMenu() }

    // listen for savedStateHandle refresh
    LaunchedEffect(navController) {
        val entry = navController.currentBackStackEntry
        entry?.savedStateHandle
            ?.getLiveData<Boolean>("menu_refresh")
            ?.observe(entry) { refresh ->
                if (refresh == true) {
                    Log.d("AdminMenuInventory", "menu_refresh -> reloading")
                    vm.loadMenu()
                    entry.savedStateHandle.remove<Boolean>("menu_refresh")
                }
            }
    }

    Scaffold(
        // The entire content is scrollable via LazyColumn
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- 1. Green Header Bar (Increased Top Padding) ---
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF00A651))
                        // Increased padding for height adjustment
                        .padding(top = 24.dp, bottom = 24.dp, start = 16.dp, end = 16.dp)
                ) {
                    Column {
                        Text("Menu & Inventory", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("Manage your kitchen offerings", color = Color.White.copy(alpha = 0.95f), fontSize = 14.sp)
                    }
                }
            }

            // --- 2. Alerts (Low Stock Alert) ---
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF6D00))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Low Stock Alert", fontWeight = FontWeight.SemiBold, color = Color(0xFFFF6D00))
                            Text("You're running low on Chicken Biryani — prepare more before lunch rush!", fontSize = 13.sp)
                            Text("1 items need restocking", color = Color(0xFFFF6D00).copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- 3. Demand Forecast Alert ---
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E8FF)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF9C27B0))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Demand Forecast", fontWeight = FontWeight.SemiBold, color = Color(0xFF9C27B0))
                            Text("Paneer Biryani orders spike on Fridays. Prepare 35 portions.", fontSize = 13.sp)
                            Text("Based on 12 weeks of historical data", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            // --- 4. Add New Food Item Button (Full Width, Green) ---
            item {
                Button(
                    onClick = { navController.navigate(Screen.AdminAddFood.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A651)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Add New Food Item", color = Color.White)
                }
            }

            // --- 5. Category Tabs (Pill style indicator on gray background - NOW FIXED, NOT SCROLLABLE) ---
            item {
                // Outer Box to provide the light gray background for the entire tabs area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(12.dp)) // Light Gray Background for the whole section
                ) {
                    // CHANGED FROM ScrollableTabRow to standard TabRow
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent, // Transparent to show Box background
                        modifier = Modifier.height(48.dp),
                        divider = {},
                        // Custom Indicator: White, rounded Box behind the selected tab
                        indicator = { tabPositions ->
                            if (selectedTabIndex < tabPositions.size) {
                                Box(
                                    Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                        .fillMaxHeight()
                                        .padding(4.dp) // Padding controls the space around the white pill
                                        .padding(horizontal = 2.dp) // Removed extra horizontal padding to spread tabs evenly
                                        .clip(RoundedCornerShape(10.dp)) // Rounded corners for the white background
                                        .background(Color.White) // White background color for the selected tab
                                        .zIndex(-1f) // Push the background behind the text
                                )
                            }
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTabIndex == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        title,
                                        color = if (isSelected) Color.Black else Color.Gray,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                },
                                selectedContentColor = Color.Black,
                                unselectedContentColor = Color.Gray
                            )
                        }
                    }
                }
                // Small vertical spacer after the tabs area
                Spacer(modifier = Modifier.height(8.dp))
            }

            // --- 6. Menu Item List ---
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (!error.isNullOrEmpty()) {
                item {
                    Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
                }
            } else if (items.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("No menu items yet")
                    }
                }
            } else {
                items(items) { item ->
                    // Apply horizontal padding here for the list items
                    AdminMenuItemCard(
                        item = item,
                        onEdit = {
                            val idStr = item.id ?: return@AdminMenuItemCard
                            navController.navigate("admin_edit_food/$idStr")
                        },

                        onDelete = {
                            val idInt = item.id?.toIntOrNull()
                            if (idInt != null) { vm.deleteMenuItem(idInt) }
                        },
                        onToggleAvailability = { newState ->
                            val idInt = item.id?.toIntOrNull()
                            if (idInt != null) { vm.toggleAvailability(idInt, if (newState) 1 else 0) }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

/** * Customized Menu Item Card (remains unchanged) */
@Composable
fun AdminMenuItemCard(
    item: MenuItemDto,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleAvailability: (Boolean) -> Unit = {}
) {
    val ctx = LocalContext.current
    val available = (item.is_available?.toIntOrNull() ?: 0) == 1
    val priceText = item.price?.toDoubleOrNull()?.let { "₹${it.toInt()}" } ?: "₹ -"

    val resolvedImage: String? = run {
        val full = item.image_full?.takeIf { it.isNotBlank() }
        if (!full.isNullOrBlank()) return@run full

        val rel = item.image_url?.trim()?.takeIf { it.isNotBlank() }
        if (rel == null) return@run null

        if (rel.startsWith("http://", true) || rel.startsWith("https://", true)) {
            if (rel.contains("://localhost") || rel.contains("://127.0.0.1")) {
                val path = rel.substringAfter("://").substringAfter('/')
                return@run NetworkClient.BASE_URL.trimEnd('/') + "/" + path
            }
            return@run rel
        }
        NetworkClient.BASE_URL.trimEnd('/') + "/" + rel.trimStart('/')
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable { /* ... */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            val imageModifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFEFEFEF))

            if (!resolvedImage.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(resolvedImage).crossfade(true).build(),
                    contentDescription = item.name,
                    modifier = imageModifier,
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = imageModifier, contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "No Image",
                        tint = Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name ?: "Item Name", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Text(text = item.category ?: "Main Course", color = Color.Gray, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 🔥 STOCK DISPLAY
                    val stockCount = item.stock?.toIntOrNull() ?: 10 // Backend value
                    
                    Text(
                        text = "Stock: $stockCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stockCount <= 5) Color.Red else Color.Black,
                        modifier = Modifier
                            .background(
                                if (stockCount <= 5) Color(0xFFFFEBEE) else Color(0xFFE0E0E0), 
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    if (stockCount <= 5) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Warning, contentDescription = "Low Stock", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp), tint = Color(0xFF00A651))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", fontSize = 13.sp, color = Color(0xFF00A651))
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = priceText, color = Color(0xFF00A651), fontWeight = FontWeight.Bold, fontSize = 16.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Add, contentDescription = "Rating", tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Text(" 4.8", fontSize = 13.sp, color = Color.Gray)
                }

                Spacer(Modifier.height(10.dp))

                Switch(
                    checked = available,
                    onCheckedChange = onToggleAvailability,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF00A651),
                        uncheckedTrackColor = Color.LightGray
                    ),
                    modifier = Modifier.padding(0.dp)
                )
                Text(text = if (available) "Available" else "Out", color = if (available) Color(0xFF00A651) else Color.Red, fontSize = 12.sp)

                Spacer(Modifier.height(10.dp))

                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}


//package com.example.floatingflavors.app.feature.admin.presentation.menu
//
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.derivedStateOf
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.example.floatingflavors.app.core.network.NetworkClient
//import com.example.floatingflavors.app.core.navigation.Screen
//import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
//import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel
//
///**
// * Admin Menu & Inventory (fixed type conversions)
// */
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AdminMenuInventoryScreen(
//    navController: NavController,
//    vm: MenuViewModel = viewModel()
//) {
//    val items by remember { derivedStateOf { vm.menuItems } }
//    val isLoading by remember { derivedStateOf { vm.isLoading } }
//    val error by remember { derivedStateOf { vm.errorMessage } }
//
//    // load menu once
//    LaunchedEffect(Unit) {
//        vm.loadMenu()
//    }
//
//    // listen for savedStateHandle refresh
//    LaunchedEffect(navController) {
//        val entry = navController.currentBackStackEntry
//        entry?.savedStateHandle
//            ?.getLiveData<Boolean>("menu_refresh")
//            ?.observe(entry) { refresh ->
//                if (refresh == true) {
//                    Log.d("AdminMenuInventory", "menu_refresh -> reloading")
//                    vm.loadMenu()
//                    entry.savedStateHandle.remove<Boolean>("menu_refresh")
//                }
//            }
//    }
//
//    Scaffold(
//        topBar = {
//            CenterAlignedTopAppBar(title = { Text("Menu & Inventory") })
//        },
//        floatingActionButton = {
//            ExtendedFloatingActionButton(
//                onClick = { navController.navigate(Screen.AdminAddFood.route) },
//                shape = RoundedCornerShape(16.dp)
//            ) {
//                Text("+ Add New Food")
//            }
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(12.dp)
//        ) {
//            when {
//                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    CircularProgressIndicator()
//                }
//
//                !error.isNullOrEmpty() -> {
//                    Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
//                }
//
//                items.isEmpty() -> {
//                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                        Text("No menu items yet")
//                    }
//                }
//
//                else -> {
//                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                        items(items) { item ->
//                            AdminMenuRow(
//                                item = item,
//                                onEdit = {
//                                    Log.d("AdminMenuInventory", "Edit clicked id=${item.id}")
//                                },
//                                onDelete = {
//                                    // convert id string -> int safely
//                                    val idInt = item.id?.toIntOrNull()
//                                    if (idInt != null) {
//                                        vm.deleteMenuItem(idInt)
//                                    } else {
//                                        Log.w("AdminMenuInventory", "delete: id null or invalid (${item.id})")
//                                    }
//                                },
//                                onToggleAvailability = { newState ->
//                                    val idInt = item.id?.toIntOrNull()
//                                    if (idInt != null) {
//                                        vm.toggleAvailability(idInt, if (newState) 1 else 0)
//                                    } else {
//                                        Log.w("AdminMenuInventory", "toggle: id null or invalid (${item.id})")
//                                    }
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AdminMenuRow(
//    item: MenuItemDto,
//    onEdit: () -> Unit = {},
//    onDelete: () -> Unit = {},
//    onToggleAvailability: (Boolean) -> Unit = {}
//) {
//    val ctx = LocalContext.current
//
//    // Resolve image: prefer `image_full` (backend), else build from BASE_URL + image_url
//    val resolvedImage: String? = run {
//        val full = item.image_full?.takeIf { it.isNotBlank() }
//        if (!full.isNullOrBlank()) return@run full
//
//        val rel = item.image_url?.trim()?.takeIf { it.isNotBlank() }
//        if (rel == null) return@run null
//
//        // If already absolute, return it (but if it uses localhost, replace host with NetworkClient BASE_URL host)
//        if (rel.startsWith("http://", true) || rel.startsWith("https://", true)) {
//            // handle legacy 'http://localhost...' by switching to the current NetworkClient base host
//            if (rel.contains("://localhost") || rel.contains("://127.0.0.1")) {
//                // extract path after host (crudely)
//                val path = rel.substringAfter("://").substringAfter('/')
//                return@run NetworkClient.BASE_URL.trimEnd('/') + "/" + path
//            }
//            return@run rel
//        }
//
//        // relative path -> prefix with BASE_URL
//        NetworkClient.BASE_URL.trimEnd('/') + "/" + rel.trimStart('/')
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { /* optionally open detail */ },
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
//            // Image
//            if (!resolvedImage.isNullOrBlank()) {
//                AsyncImage(
//                    model = ImageRequest.Builder(ctx).data(resolvedImage).crossfade(true).build(),
//                    contentDescription = item.name,
//                    modifier = Modifier
//                        .size(84.dp)
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color(0xFFF2F2F2))
//                )
//            } else {
//                Box(
//                    modifier = Modifier
//                        .size(84.dp)
//                        .clip(RoundedCornerShape(8.dp))
//                        .background(Color(0xFFEFEFEF)),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("No\nImage", style = MaterialTheme.typography.bodySmall)
//                }
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(modifier = Modifier.weight(1f)) {
//                Text(text = item.name ?: "-", style = MaterialTheme.typography.titleMedium)
//                Spacer(Modifier.height(4.dp))
//                Text(text = item.category ?: "-", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
//                Spacer(Modifier.height(6.dp))
//                Text(text = "₹ ${item.price ?: "-"}", style = MaterialTheme.typography.titleSmall, color = Color(0xFF0AAC3F))
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
//                // convert is_available string -> Int
//                val available = (item.is_available?.toIntOrNull() ?: 0) == 1
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(text = if (available) "Available" else "Out", style = MaterialTheme.typography.bodySmall)
//                    Spacer(Modifier.width(6.dp))
//                    Switch(
//                        checked = available,
//                        onCheckedChange = onToggleAvailability,
//                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0AAC3F))
//                    )
//                }
//
//                Spacer(Modifier.height(8.dp))
//
//                OutlinedButton(onClick = onEdit, modifier = Modifier.width(84.dp)) {
//                    Text("Edit", style = MaterialTheme.typography.bodySmall)
//                }
//
//                Spacer(Modifier.height(8.dp))
//
//                OutlinedButton(
//                    onClick = onDelete,
//                    modifier = Modifier.width(84.dp),
//                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
//                ) {
//                    Text("Delete", style = MaterialTheme.typography.bodySmall)
//                }
//            }
//        }
//    }
//}
