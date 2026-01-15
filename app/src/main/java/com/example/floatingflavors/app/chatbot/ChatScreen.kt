package com.example.floatingflavors.app.chatbot

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.floatingflavors.app.chatbot.components.*
import com.example.floatingflavors.app.chatbot.model.ChatViewModel

@Composable
fun ChatScreen(
    userId: Int,
    viewModel: ChatViewModel
) {
    val messages by viewModel.messages.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val events by viewModel.events.collectAsState()
    val corporates by viewModel.corporates.collectAsState()

    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // 🔹 HEADER
        ChatHeader()

        // 🔹 CHAT BODY
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState
        ) {

            items(messages) {
                ChatBubble(it)
            }

            if (menuItems.isNotEmpty()) {
                item {
                    Text("Menu Results", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp))
                }
                items(menuItems) { MenuCard(it) }
            }

            if (orders.isNotEmpty()) {
                item { Text("Your Orders", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp)) }
                items(orders) {
                    Text("🧾 Order #${it.id} • ${it.status} • ₹${it.amount}", modifier = Modifier.padding(8.dp))
                }
            }

            if (events.isNotEmpty()) {
                item { Text("Your Events", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp)) }
                items(events) {
                    Text("${it.event_name} • ${it.event_date}", modifier = Modifier.padding(8.dp))
                }
            }

            if (corporates.isNotEmpty()) {
                item { Text("Corporate Bookings", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(8.dp)) }
                items(corporates) {
                    Text("${it.company_name} • ${it.status}", modifier = Modifier.padding(8.dp))
                }
            }
        }

        // 🔹 INPUT BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {

            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask food, track order, book event…") },
                singleLine = true
            )

            IconButton(
                enabled = input.isNotBlank(),
                onClick = {
                    viewModel.sendMessage(userId, input.trim())
                    input = ""
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}
