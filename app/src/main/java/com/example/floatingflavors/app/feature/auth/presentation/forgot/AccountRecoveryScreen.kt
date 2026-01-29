package com.example.floatingflavors.app.feature.auth.presentation.forgot

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AccountRecoveryScreen(
    loading: Boolean,
    message: String?,
    onBack: () -> Unit,
    onSendOtp: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val tilt by animateFloatAsState(-8f, label = "tilt")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(listOf(Color(0xFFFFEFE3), Color.White))
            )
            .padding(20.dp)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            IconButton(onClick = onBack) {
                Surface(shape = CircleShape, color = Color.White) {
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.padding(8.dp))
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                "SECURITY",
                letterSpacing = 2.sp,
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(40.dp))

            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .rotate(tilt)
                            .background(Color(0xFFFFE0CC), RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Email, null, tint = Color(0xFFFF7A18), modifier = Modifier.size(40.dp))
                        Icon(
                            Icons.Default.Security,
                            null,
                            tint = Color(0xFFFF7A18),
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    Text("Reset your password", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Enter your email to receive a verification code.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(28.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        placeholder = { Text("name@example.com") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = { onSendOtp(email.trim()) },
                        enabled = email.trim().contains("@") && !loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        if (email.contains("@") && !loading)
                                            listOf(Color(0xFFFF7A18), Color(0xFFFF9A3C)) // Active Orange
                                        else
                                            listOf(Color.Gray, Color.LightGray) // Disabled
                                    ),
                                    RoundedCornerShape(50)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Send OTP →",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Suddenly remembered? ")
                Text(
                    "Login",
                    color = Color(0xFFFF7A18),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
        }

        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}
