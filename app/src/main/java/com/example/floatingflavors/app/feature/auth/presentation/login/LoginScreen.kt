package com.example.floatingflavors.app.feature.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onBackClick: () -> Unit,
    onLoginSuccess: (role: String) -> Unit,
    onNavigateToRegister : () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Role dropdown state
    val roles = listOf("User", "Admin")
    var selectedRole by remember { mutableStateOf("Select") }
    var roleMenuExpanded by remember { mutableStateOf(false) }

    var rememberMe by remember { mutableStateOf(false) }

    // Error message for login failures
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Dummy credentials (in-code, simple way)
    val adminEmail = "admin@gmail.com"
    val adminPassword = "123"

    val dummyUsers = listOf(
        "user1@gmail.com" to "123",
        "user2@gmail.com" to "123",
        "user3@gmail.com" to "123"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8FFD1)) // light green bg like your Figma
            .padding(top = 16.dp)
    ) {
        // Back arrow
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Login",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign in to order and book catering",
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // White card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                // Email
                Text(
                    text = "Email Address",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your email") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password
                Text(
                    text = "Password",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role
                Text(
                    text = "Role",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))

                Box {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { roleMenuExpanded = true },
                        enabled = false, // user cannot type manually
                        placeholder = { Text("Select") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select role"
                            )
                        }
                    )

                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    selectedRole = role
                                    roleMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Remember + Forgot password row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text("Remember me")

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = { /* TODO: forgot password */ }) {
                        Text("Forgot Password?")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Login button with role-based dummy check
                Button(
                    onClick = {
                        when (selectedRole) {
                            "Admin" -> {
                                if (email == adminEmail && password == adminPassword) {
                                    errorMessage = null
                                    onLoginSuccess("Admin")
                                } else {
                                    errorMessage = "Invalid admin credentials"
                                }
                            }

                            "User" -> {
                                val validUser =
                                    dummyUsers.any { it.first == email && it.second == password }
                                if (validUser) {
                                    errorMessage = null
                                    onLoginSuccess("User")
                                } else {
                                    errorMessage = "Invalid user credentials"
                                }
                            }

                            else -> {
                                errorMessage = "Please select a role"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Login")
                }

                // Error message
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Register row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Don't have an account? ",
                        fontSize = 13.sp)
                    Text(
                        text = "Register",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}
