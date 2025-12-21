package com.example.floatingflavors.app.feature.user.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.app.core.network.NetworkClient

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    onNavigateTerms: () -> Unit,
    onNavigatePrivacy: () -> Unit,
    onSavedAddressClick: () -> Unit,

    ) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.loadSettings() // 🔥 refresh when coming back
        }
    }


    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        topBar = {}, // AdminOrders-style (no shifting header)
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->

        val startPad = innerPadding.calculateStartPadding(layoutDirection)
        val endPad = innerPadding.calculateEndPadding(layoutDirection)
        val bottomPad = innerPadding.calculateBottomPadding()

        when (val state = viewModel.uiState) {

            is SettingsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is SettingsUiState.Success -> {
                val data = state.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = startPad, end = endPad, bottom = bottomPad)
                        .verticalScroll(rememberScrollState())
                ) {

                    /* ---------------- HEADER ---------------- */

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 64.dp, max = 72.dp)
                            .background(Color.White)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Settings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    /* ---------------- PROFILE CARD ---------------- */

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFE0B2)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = NetworkClient.BASE_URL + "/" + data.profile_image,
                                        contentDescription = "Profile Image",
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
//                                    Icon(
//                                        Icons.Default.Person,
//                                        contentDescription = null,
//                                        tint = Color(0xFFFF9800),
//                                        modifier = Modifier.size(30.dp)
//                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column {
                                    Text(data.name ?: "", fontSize = 16.sp)
                                    Text(
                                        data.email ?: "",
                                        fontSize = 13.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFF3E0))
                                    .clickable { },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.clickable { onEditProfileClick() }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    /* ---------------- ACCOUNT ---------------- */

                    SectionTitle("ACCOUNT")
                    SettingsCard {
                        SettingsRow(
                            Icons.Default.ShoppingBag,
                            "My Orders",
                            iconBg = Color(0xFFEAF2FF),
                            iconTint = Color(0xFF4F8DF7)
                        )
                        DarkDivider()
                        SettingsRow(
                            Icons.Default.CreditCard,
                            "Payment Methods",
                            iconBg = Color(0xFFE9F8EF),
                            iconTint = Color(0xFF22C55E)
                        )
                        DarkDivider()
                        SettingsRow(
                            Icons.Default.LocationOn,
                            "Saved Addresses",
                            iconBg = Color(0xFFF1ECFF),
                            iconTint = Color(0xFF8B5CF6),
                            onClick = onSavedAddressClick
                        )
                    }

                    /* ---------------- PREFERENCES ---------------- */

                    SectionTitle("PREFERENCES")
                    SettingsCard {
                        SettingsRow(
                            Icons.Default.Notifications,
                            "Notifications",
                            iconBg = Color(0xFFFFF7E6),
                            iconTint = Color(0xFFF59E0B)
                        )
                        DarkDivider()
                        SettingsRow(
                            Icons.Default.Language,
                            "Language",
                            trailingText = data.language ?: "English",
                            iconBg = Color(0xFFE6FBFF),
                            iconTint = Color(0xFF06B6D4)
                        )
                    }

                    /* ---------------- SUPPORT ---------------- */

                    SectionTitle("SUPPORT & INFORMATION")
                    SettingsCard {
                        SettingsRow(
                            Icons.Default.Help,
                            "Help & Support",
                            iconBg = Color(0xFFEEF2FF),
                            iconTint = Color(0xFF6366F1)
                        )
                        DarkDivider()
                        SettingsRow(
                            Icons.Default.Info,
                            "How to Use",
                            iconBg = Color(0xFFEEF2FF),
                            iconTint = Color(0xFF6366F1)
                        )
                        DarkDivider()
                        SettingsRow(
                            Icons.Default.Description,
                            "Terms of Service",
                            iconBg = Color(0xFFEEF2FF),
                            iconTint = Color(0xFF6366F1),
                            onClick = onNavigateTerms
                        )
                        DarkDivider()
                        SettingsRow(
                            Icons.Default.VerifiedUser,
                            "Privacy Policy",
                            iconBg = Color(0xFFEEF2FF),
                            iconTint = Color(0xFF6366F1),
                            onClick = onNavigatePrivacy
                        )
                    }

                    /* ---------------- LOGOUT / DELETE ---------------- */

                    SettingsCard {
                        SettingsRow(
                            Icons.Default.Logout,
                            "Log out",
                            iconBg = Color(0xFFFEECEC),
                            iconTint = Color(0xFFEF4444),
                            textColor = Color(0xFFEF4444),
                            showChevron = false,
                            onClick = { viewModel.logout() }
                        )
                        DarkDivider()
                        SettingsRow(
                            Icons.Default.DeleteForever,
                            "Delete Account",
                            iconBg = Color(0xFFFEECEC),
                            iconTint = Color(0xFFEF4444),
                            textColor = Color(0xFFEF4444),
                            showChevron = false,
                            onClick = { showDeleteDialog = true }
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    /* ---------------- FOOTER ---------------- */

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Floating Flavors App", fontSize = 12.sp, color = Color.Gray)
                        Text(
                            "Version 2.4.0 (Build 302)",
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }

            is SettingsUiState.Error -> {
                Text(
                    state.message.ifEmpty { "Something went wrong" },
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    /* ---------------- DELETE DIALOG ---------------- */

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete your account?\nThis action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteAccount()
                }) {
                    Text("Yes", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

/* ---------------- HELPERS ---------------- */

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        color = Color.Gray,
        modifier = Modifier.padding(start = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun DarkDivider() {
    Divider(
        color = Color(0xFFE5E7EB),
        thickness = 1.dp
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    trailingText: String? = null,
    iconBg: Color,
    iconTint: Color,
    textColor: Color = Color.Black,
    showChevron: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Spacer(Modifier.width(12.dp))
            Text(title, color = textColor)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            trailingText?.let {
                Text(it, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.width(6.dp))
            }
            if (showChevron) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        }
    }
}
