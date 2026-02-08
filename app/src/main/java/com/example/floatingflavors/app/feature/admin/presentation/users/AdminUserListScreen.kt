package com.example.floatingflavors.app.feature.admin.presentation.users

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.floatingflavors.app.feature.admin.data.remote.AdminApi
import com.example.floatingflavors.app.feature.admin.data.remote.AdminRepository
import com.example.floatingflavors.app.feature.admin.data.remote.UserDto
import com.example.floatingflavors.app.core.network.NetworkClient
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdminUserListScreen(
    navController: NavController
) {
    // 1. Manual DI (Since we don't have Hilt yet)
    val context = LocalContext.current
    val api = remember { NetworkClient.retrofit.create(AdminApi::class.java) }
    val repository = remember { AdminRepository(api) }
    val viewModel: AdminUserListViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AdminUserListViewModel(repository) as T
            }
        }
    )

    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    // Filter Logic
    val filteredUsers = users.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
        it.email.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF00C853)) // Admin Primary Green
                    .padding(bottom = 16.dp)
            ) {
                // Title
                Text(
                    "User Management",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                    modifier = Modifier.padding(top = 48.dp, start = 16.dp, end = 16.dp)
                )
                
                Spacer(Modifier.height(16.dp))

                // Custom Tabs
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(4.dp)
                ) {
                    TabButton("Users", selectedRole == "User", Modifier.weight(1f)) {
                        viewModel.onRoleSelected("User")
                    }
                    TabButton("Delivery Partners", selectedRole == "Delivery", Modifier.weight(1f)) {
                        viewModel.onRoleSelected("Delivery")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            
            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search by name or email...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(0.dp, Color.Transparent),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) }
            )

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00C853))
                }
            } else if (filteredUsers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SearchOff, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No results found", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers) { user ->
                        UserCard(
                            user = user,
                            onCall = {
                                val uri = Uri.parse("tel:${user.phone ?: ""}")
                                val intent = Intent(Intent.ACTION_DIAL, uri)
                                context.startActivity(intent)
                            },
                            onClick = {
                                navController.navigate(com.example.floatingflavors.app.core.navigation.Screen.AdminUserDetails.createRoute(user.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) Color.White else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (isSelected) Color(0xFF00C853) else Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun UserCard(user: UserDto, onCall: () -> Unit, onClick: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    androidx.compose.animation.AnimatedVisibility(
        visible = isVisible,
        enter = androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(500)) + 
                androidx.compose.animation.slideInVertically(androidx.compose.animation.core.tween(500)) { it / 3 }
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { onClick() }
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (user.role == "Delivery") 
                                    listOf(Color(0xFFFF9800), Color(0xFFFFCC80)) 
                                    else 
                                    listOf(Color(0xFF2196F3), Color(0xFF64B5F6))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.profile_image_full.isNullOrBlank()) {
                         AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.profile_image_full)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = user.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Info
                Column(Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    
                    Spacer(Modifier.height(6.dp))
                    // Role Badge
                    Surface(
                        color = if (user.role == "Delivery") Color(0xFFFFF3E0) else Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = user.role.uppercase(),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (user.role == "Delivery") Color(0xFFEF6C00) else Color(0xFF1565C0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Action (Call)
                if (!user.phone.isNullOrBlank()) {
                    IconButton(
                        onClick = onCall,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE8F5E9), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.Call, 
                            contentDescription = "Call",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    // Chevron for detail hint
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = Color.LightGray
                    )
                }
            }
        }
    }
}
