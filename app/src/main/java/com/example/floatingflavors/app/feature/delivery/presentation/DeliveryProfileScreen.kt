package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DeliveryProfileScreen(onLogout: () -> Unit) {

    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            ProfileBottomNavigationBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
            )

            // Profile Header Card
            ProfileHeaderCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Contact Info Card
            ProfileContactInfoCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Account Settings Card
            ProfileAccountSettingsCard()

            Spacer(modifier = Modifier.height(24.dp))

            // Log Out Button
            ProfileLogOutButton(
                onClick = { showLogoutDialog = true }
            )


            Spacer(modifier = Modifier.weight(1f))

            // Version Info
            ProfileVersionInfo()
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
                ) {
                    Text("Yes, Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileHeaderCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFE0E0E0), CircleShape)
                    .border(3.dp, Color(0xFF4CAF50), CircleShape)
            ) {
                Text(
                    text = "AJ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Alex Johnson",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: #FF-9281",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = " • ",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "4.9 ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rating",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileContactInfoCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Phone Number
            ProfileContactInfoRow(
                title = "PHONE NUMBER",
                value = "+1 (555) 019-2834",
                icon = Icons.Default.Phone
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email Address
            ProfileContactInfoRow(
                title = "EMAIL ADDRESS",
                value = "alex.driver@example.com",
                icon = Icons.Default.Email
            )
        }
    }
}

@Composable
fun ProfileContactInfoRow(
    title: String,
    value: String,
    icon: ImageVector
) {
    Column {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ProfileAccountSettingsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
        ) {
            Text(
                text = "ACCOUNT SETTINGS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Items
            ProfileSettingsItem(
                icon = "✔",
                title = "Edit Profile",
                description = "Update name & details",
                hasArrow = true
            )

            ProfileSettingsItem(
                icon = "✘",
                title = "Vehicle Information",
                description = "Toyota Prius – ABC 123",
                hasArrow = true
            )

            ProfileSettingsItem(
                icon = "✘",
                title = "Documents",
                description = "License, Insurance",
                hasArrow = true
            )

            ProfileSettingsItem(
                icon = "✘",
                title = "Liaia & Cimmer",
                description = "",
                hasArrow = true
            )
        }
    }
}

@Composable
fun ProfileSettingsItem(
    icon: String,
    title: String,
    description: String,
    hasArrow: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(vertical = 12.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color(0xFFF5F5F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = icon,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }

        if (hasArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProfileLogOutButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Log Out",
                tint = Color(0xFFD32F2F),
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Log Out",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
fun ProfileVersionInfo() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Version 1.0.4 • Build 2023.10",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileBottomNavigationBar() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileIconWithLabel(
                icon = Icons.Default.Dashboard,
                label = "Dashboard",
                isSelected = false
            )

            ProfileIconWithLabel(
                icon = Icons.Default.ListAlt,
                label = "Orders",
                isSelected = false
            )

            ProfileIconWithLabel(
                icon = Icons.Default.AttachMoney,
                label = "Earnings",
                isSelected = false
            )

            ProfileIconWithLabel(
                icon = Icons.Default.Person,
                label = "Profile",
                isSelected = true
            )
        }
    }
}

@Composable
fun ProfileIconWithLabel(
    icon: ImageVector,
    label: String,
    isSelected: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFF4CAF50) else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color(0xFF4CAF50) else Color.Gray
        )
    }
}