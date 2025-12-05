package com.example.floatingflavors.app.feature.admin.presentation.menu

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.core.navigation.Screen
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel

/**
 * Admin Menu & Inventory (fixed type conversions)
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

    // load menu once
    LaunchedEffect(Unit) {
        vm.loadMenu()
    }

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
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Menu & Inventory") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AdminAddFood.route) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("+ Add New Food")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            when {
                isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                !error.isNullOrEmpty() -> {
                    Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                }

                items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No menu items yet")
                    }
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(items) { item ->
                            AdminMenuRow(
                                item = item,
                                onEdit = {
                                    Log.d("AdminMenuInventory", "Edit clicked id=${item.id}")
                                },
                                onDelete = {
                                    // convert id string -> int safely
                                    val idInt = item.id?.toIntOrNull()
                                    if (idInt != null) {
                                        vm.deleteMenuItem(idInt)
                                    } else {
                                        Log.w("AdminMenuInventory", "delete: id null or invalid (${item.id})")
                                    }
                                },
                                onToggleAvailability = { newState ->
                                    val idInt = item.id?.toIntOrNull()
                                    if (idInt != null) {
                                        vm.toggleAvailability(idInt, if (newState) 1 else 0)
                                    } else {
                                        Log.w("AdminMenuInventory", "toggle: id null or invalid (${item.id})")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMenuRow(
    item: MenuItemDto,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleAvailability: (Boolean) -> Unit = {}
) {
    val ctx = LocalContext.current

    // Resolve image: prefer `image_full` (backend), else build from BASE_URL + image_url
    val resolvedImage: String? = run {
        val full = item.image_full?.takeIf { it.isNotBlank() }
        if (!full.isNullOrBlank()) return@run full

        val rel = item.image_url?.trim()?.takeIf { it.isNotBlank() }
        if (rel == null) return@run null

        // If already absolute, return it (but if it uses localhost, replace host with NetworkClient BASE_URL host)
        if (rel.startsWith("http://", true) || rel.startsWith("https://", true)) {
            // handle legacy 'http://localhost...' by switching to the current NetworkClient base host
            if (rel.contains("://localhost") || rel.contains("://127.0.0.1")) {
                // extract path after host (crudely)
                val path = rel.substringAfter("://").substringAfter('/')
                return@run NetworkClient.BASE_URL.trimEnd('/') + "/" + path
            }
            return@run rel
        }

        // relative path -> prefix with BASE_URL
        NetworkClient.BASE_URL.trimEnd('/') + "/" + rel.trimStart('/')
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* optionally open detail */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Image
            if (!resolvedImage.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(resolvedImage).crossfade(true).build(),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF2F2F2))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEFEFEF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No\nImage", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.name ?: "-", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(text = item.category ?: "-", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                Text(text = "₹ ${item.price ?: "-"}", style = MaterialTheme.typography.titleSmall, color = Color(0xFF0AAC3F))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                // convert is_available string -> Int
                val available = (item.is_available?.toIntOrNull() ?: 0) == 1
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (available) "Available" else "Out", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.width(6.dp))
                    Switch(
                        checked = available,
                        onCheckedChange = onToggleAvailability,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF0AAC3F))
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(onClick = onEdit, modifier = Modifier.width(84.dp)) {
                    Text("Edit", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.width(84.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
