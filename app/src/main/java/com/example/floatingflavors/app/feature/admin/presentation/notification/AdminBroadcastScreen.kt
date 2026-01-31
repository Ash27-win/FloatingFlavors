package com.example.floatingflavors.app.feature.admin.presentation.notification

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.notification.data.NotificationRepository
import com.example.floatingflavors.app.core.data.local.AppDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBroadcastScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Manual DI
    val repository = remember {
        NotificationRepository(
            NetworkClient.notificationApi,
            AppDatabase.getDatabase(context)
        )
    }
    val factory = object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
            return AdminBroadcastViewModel(repository) as T
        }
    }
    val viewModel: AdminBroadcastViewModel = viewModel(factory = factory)
    
    val state by viewModel.state.collectAsState()
    
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("User") }
    
    LaunchedEffect(state) {
        when(state) {
            is BroadcastState.Success -> {
                Toast.makeText(context, (state as BroadcastState.Success).message, Toast.LENGTH_SHORT).show()
                viewModel.resetState()
                onBackClick()
            }
            is BroadcastState.Error -> {
                Toast.makeText(context, (state as BroadcastState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Broadcast Message") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2563EB),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // Icon Header
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = Color(0xFF2563EB)
                )
            }
            
            Text(
                "Send a marketing message or alert to all users (or delivery partners).",
                fontSize = 14.sp,
                color = Color.Gray
            )

            // Target Role
            Text("Target Audience", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf("User", "Delivery").forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        label = { Text(role) },
                        leadingIcon = {
                             if (selectedRole == role) {
                                 Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                             }
                        }
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g. 50% Off Today!") }
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Message Body") },
                modifier = Modifier.fillMaxWidth().height(150.dp),
                placeholder = { Text("Type your message here...") }
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { viewModel.sendBroadcast(title, body, selectedRole) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = state !is BroadcastState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                if (state is BroadcastState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Send Broadcast", fontSize = 16.sp)
                }
            }
        }
    }
}
