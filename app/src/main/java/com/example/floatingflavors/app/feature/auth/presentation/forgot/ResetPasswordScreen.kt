package com.example.floatingflavors.app.feature.auth.presentation.forgot

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ResetPasswordScreen(
    loading: Boolean,
    message: String?,
    onBack: () -> Unit,
    onPasswordUpdated: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    val hasLength = password.length >= 8
    val hasSymbol = password.any { !it.isLetterOrDigit() }
    val strength = when {
        !hasLength -> 1
        hasLength && !hasSymbol -> 2
        else -> 3
    }

    val fill by animateFloatAsState(strength / 3f, label = "fill")
    val color by animateColorAsState(
        if (strength == 3) Color(0xFF2ECC71) else Color(0xFFFFA000),
        label = "color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFFFFEFE3), Color.White)))
            .padding(20.dp)
    ) {

        Column {

            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }

            Text("Reset Password", fontSize = 26.sp, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                leadingIcon = { Icon(Icons.Default.Key, null) },
                trailingIcon = {
                    Icon(
                        if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        modifier = Modifier.clickable { visible = !visible }
                    )
                },
                visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Color.LightGray, RoundedCornerShape(50))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fill)
                        .height(6.dp)
                        .background(color, RoundedCornerShape(50))
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it },
                placeholder = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { onPasswordUpdated(password) },
                enabled = strength == 3 && password == confirm,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(Color(0xFFFF7A18), Color(0xFFFF3C3C))),
                            RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Update Password →", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Back to Login Screen",
                color = Color(0xFFFF7A18),
                modifier = Modifier.align(Alignment.CenterHorizontally).clickable { onLoginClick() }
            )
        }
    }
}
