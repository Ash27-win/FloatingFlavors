package com.example.floatingflavors.app.feature.user.presentation.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.example.floatingflavors.app.core.network.NetworkClient
import android.util.Log

// helper to resolve image URL
private fun resolvedImageUrl(item: MenuItemDto, fallbackBase: String): String? {
    item.image_full?.takeIf { it.isNotBlank() }?.let { return it }

    val rel = item.image_url?.trim()?.takeIf { it.isNotBlank() } ?: return null

    // if absolute already:
    if (rel.startsWith("http://", true) || rel.startsWith("https://", true)) {
        // if it uses localhost or 127.0.0.1, replace host with fallbackBase host
        if (rel.contains("://localhost") || rel.contains("://127.0.0.1")) {
            // crude: get path after host
            val path = rel.substringAfter("://").substringAfter('/')
            return fallbackBase.trimEnd('/') + "/" + path
        }
        return rel
    }

    // else relative path -> fallback base + rel
    return fallbackBase.trimEnd('/') + "/" + rel.trimStart('/')
}

@Composable
fun UserMenuGridScreen(vm: MenuViewModel = viewModel(), onItemClick: (MenuItemDto) -> Unit = {}) {
    val items by remember { derivedStateOf { vm.menuItems } }
    val isLoading by remember { derivedStateOf { vm.isLoading } }
    val error by remember { derivedStateOf { vm.errorMessage } }

    // load menu on enter
    LaunchedEffect(Unit) { vm.loadMenu() }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        Text("Our Menu", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        when {
            isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            !error.isNullOrEmpty() -> {
                Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
            }
            items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No menu items available")
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        val imageUrl = resolvedImageUrl(item, NetworkClient.BASE_URL)
                        Log.d("IMG", "resolved url = $imageUrl")
                        Card(modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable { onItemClick(item) }) {
                            Column {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = item.name,
                                    modifier = Modifier.fillMaxWidth().height(110.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(item.name ?: "-", modifier = Modifier.padding(horizontal = 8.dp))
                                Text("₹ ${item.price ?: "-"}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                            }
                        }
                    }
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
