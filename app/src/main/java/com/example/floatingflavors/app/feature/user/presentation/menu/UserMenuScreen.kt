package com.example.floatingflavors.app.feature.user.presentation.menu

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import androidx.compose.ui.text.style.TextAlign

@Composable
fun UserMenuScreen(
    vm: MenuViewModel = viewModel()
) {
    // read ViewModel state directly (Compose observes these)
    val isLoading = vm.isLoading
    val error = vm.errorMessage
    val items = vm.menuItems

    // load menu once
    LaunchedEffect(Unit) {
        vm.loadMenu()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Text("Menu", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            !error.isNullOrEmpty() -> {
                Text(error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
            }

            items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items yet")
                }
            }

            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items) { item ->
                        MenuRow(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(item: MenuItemDto) {
    val ctx = LocalContext.current
    val url = item.image_url ?: ""

    LaunchedEffect(item.id) {
        Log.d("UserMenu", "LOAD_IMAGE -> id=${item.id} url=$url")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (url.isNotBlank()) {
                val request = ImageRequest.Builder(ctx)
                    .data(url)
                    .crossfade(true)
                    .listener(
                        onSuccess = { _request, _metadata ->
                            Log.d("Coil", "Loaded image: $url")
                        },
                        onError = { _request, throwable ->
                            val type = throwable?.javaClass?.simpleName ?: "UnknownError"
                            val msg = throwable?.toString() ?: "no message"
                            Log.e("Coil", "Failed load: $url -> $type: $msg")
                        }
                    )
                    .build()

                AsyncImage(
                    model = request,
                    contentDescription = item.name,
                    modifier = Modifier.size(80.dp)
                )
            } else {
                Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No\nImage",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                item.description?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(6.dp))
                Text("₹ ${item.price}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
