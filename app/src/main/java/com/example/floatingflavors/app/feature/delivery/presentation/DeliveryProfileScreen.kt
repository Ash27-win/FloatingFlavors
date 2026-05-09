package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.feature.delivery.presentation.components.SectionHeader
import androidx.compose.ui.platform.testTag
import com.example.floatingflavors.app.core.util.TestTags
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import com.example.floatingflavors.app.feature.delivery.presentation.profile.DeliveryProfileViewModel
import com.example.floatingflavors.app.feature.delivery.data.remote.dto.DeliveryProfileDto
import com.example.floatingflavors.app.feature.delivery.presentation.profile.DeliveryProfileUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryProfileScreen(
    viewModel: DeliveryProfileViewModel = viewModel(),
    onEditProfile: () -> Unit,
    onVehicleInfo: () -> Unit,
    onDocuments: () -> Unit,
    onHelpSupport: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Reload on entry
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        containerColor = Color(0xFFFFFBF7),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Box(modifier = Modifier.fillMaxWidth().padding(end = 40.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "PARTNER PROFILE",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { /* Back Not needed here as it's the main settings hub */ },
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.ChevronLeft,
                                contentDescription = "Back",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Settings context */ },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFFBF7))
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is DeliveryProfileUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFFF6D00))
                }
                is DeliveryProfileUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Error: ${state.message}", color = Color.Red)
                        Button(onClick = { viewModel.loadProfile() }) { Text("Retry") }
                    }
                }
                is DeliveryProfileUiState.Success -> {
                    Content(
                        profile = state.profile,
                        onEditProfile = onEditProfile,
                        onVehicleInfo = onVehicleInfo,
                        onDocuments = onDocuments,
                        onHelpSupport = onHelpSupport,
                        onLogoutClick = { showLogoutDialog = true },
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    }
                ) { Text("Yes, Logout", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun Content(
    profile: DeliveryProfileDto,
    onEditProfile: () -> Unit,
    onVehicleInfo: () -> Unit,
    onDocuments: () -> Unit,
    onHelpSupport: () -> Unit,
    onLogoutClick: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TestTags.DELIVERY_PROFILE_SCREEN)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar with Ring
        Box(
            contentAlignment = Alignment.Center
        ) {
            // Outer Ring
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.White.copy(alpha = 0.5f), CircleShape)
                    .border(1.dp, Color(0xFFFFE0B2), CircleShape)
            )
            
            AsyncImage(
                model = profile.profileImage,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.White, CircleShape),
                contentScale = ContentScale.Crop
            )

            // Pro Badge
            if (profile.isVerified) {
                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 10.dp, y = (-10).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = Color(0xFFFF6D00),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("PRO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Name
        Text(
            text = profile.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tier & Rating
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = Color.Black,
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "${profile.tier.uppercase()} TIER",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Surface(
                color = Color.White,
                shape = RoundedCornerShape(50),
                shadowElevation = 2.dp
            ) {
                Row(
                   modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(text = "${profile.rating}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Contact Details
        SectionHeader("CONTACT DETAILS")
        Spacer(modifier = Modifier.height(8.dp))
        
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val scope = rememberCoroutineScope()

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Column(Modifier.padding(20.dp)) {
                ContactRow(
                    icon = Icons.Default.Phone,
                    label = "MOBILE NUMBER",
                    value = profile.phone,
                    containerColor = Color(0xFFF5F5F5),
                    onCopy = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(profile.phone))
                        scope.launch { snackbarHostState.showSnackbar("Phone number copied!") }
                    }
                )
                Spacer(Modifier.height(16.dp))
                ContactRow(
                    icon = Icons.Default.Email,
                    label = "EMAIL ADDRESS",
                    value = profile.email,
                    containerColor = Color(0xFFFFF3E0),
                    onCopy = {
                        clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(profile.email))
                        scope.launch { snackbarHostState.showSnackbar("Email address copied!") }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Account Experience
        SectionHeader("ACCOUNT EXPERIENCE")
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
        ) {
            Column(Modifier.padding(vertical = 12.dp)) {
                SettingsItem(icon = Icons.Default.Person, title = "Edit Profile", modifier = Modifier.testTag(TestTags.ITEM_EDIT_PROFILE), onClick = onEditProfile)
                SettingsItem(icon = Icons.Default.LocalShipping, title = "Vehicle Information", modifier = Modifier.testTag(TestTags.ITEM_VEHICLE_INFO), onClick = onVehicleInfo)
                SettingsItem(icon = Icons.Default.Folder, title = "Documents", modifier = Modifier.testTag(TestTags.ITEM_DOCUMENTS), onClick = onDocuments)
                SettingsItem(icon = Icons.Default.Help, title = "Help & Support", modifier = Modifier.testTag(TestTags.ITEM_HELP_SUPPORT), onClick = onHelpSupport)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Logout Button Card
         Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF5F5)), // Light red bg
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 20.dp).clickable { onLogoutClick() }
        ) {
            Row(
                Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Box(
                         modifier = Modifier.size(36.dp).background(Color.White, CircleShape),
                         contentAlignment = Alignment.Center
                     ) {
                         Icon(Icons.Default.Logout, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                     }
                     Spacer(Modifier.width(16.dp))
                     Text("Log Out", color = Color.Red, fontWeight = FontWeight.Bold)
                 }
                 Icon(Icons.Default.ChevronRight, null, tint = Color.Red.copy(alpha = 0.5f))
            }
        }

        // Bottom Navigation Spacer
        Spacer(modifier = Modifier.height(84.dp))
    }
}


@Composable
fun ContactRow(icon: ImageVector, label: String, value: String, containerColor: Color, onCopy: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
         Box(
             modifier = Modifier.size(44.dp).background(containerColor, CircleShape),
             contentAlignment = Alignment.Center
         ) {
             Icon(icon, null, tint = if(icon == Icons.Default.Email) Color(0xFFFF6D00) else Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
         }
         Spacer(Modifier.width(16.dp))
         Column(Modifier.weight(1f)) {
             Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
             Text(value, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)
         }
         IconButton(onClick = onCopy) {
             Icon(Icons.Default.ContentCopy, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
         }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
             modifier = Modifier.size(40.dp).background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp)),
             contentAlignment = Alignment.Center
         ) {
             Icon(icon, null, tint = Color(0xFF5D4037), modifier = Modifier.size(20.dp))
         }
         Spacer(Modifier.width(16.dp))
         Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black, modifier = Modifier.weight(1f))
         Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
    }
}