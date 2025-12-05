package com.example.floatingflavors.app.feature.user.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items // <- IMPORTANT import
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto

@Composable
fun UserHomeScreen(vm: MenuViewModel = viewModel(), onBrowseMenu: () -> Unit = {}) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Hello, User!", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = onBrowseMenu, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Browse Menu")
            }
            OutlinedButton(onClick = { /* booking */ }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Booking Catering")
            }
        }

        Spacer(Modifier.height(16.dp))

        Text("Featured Items", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        // make the type explicit so overload resolution is clear
        val menuItems: List<MenuItemDto> = vm.menuItems

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // disambiguate using named parameter and explicit typed list
            items(items = menuItems.take(6)) { item ->
                FeaturedCard(item = item)
            }
        }

        Spacer(Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Limited Time Offer!", modifier = Modifier.weight(1f))
                Button(onClick = {}) { Text("Get Offer") }
            }
        }
    }
}

@Composable
fun FeaturedCard(item: MenuItemDto) {
    Card(modifier = Modifier.size(width = 200.dp, height = 180.dp), shape = RoundedCornerShape(12.dp)) {
        Column {
            AsyncImage(model = item.image_url, contentDescription = item.name, modifier = Modifier.height(110.dp).fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(item.name ?: "-", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 8.dp))
            Text("₹ ${item.price ?: "-"}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
        }
    }
}

