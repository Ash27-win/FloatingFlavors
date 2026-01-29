package com.example.floatingflavors.app.feature.auth.presentation.forgot

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    // Validation
    val hasLength = password.length >= 8
    val hasSymbol = password.any { !it.isLetterOrDigit() }
    
    // Strength Logic: 0=Empty, 1=Weak, 2=Medium, 3=Strong
    val strengthLevel = when {
        password.isEmpty() -> 0
        hasLength && hasSymbol -> 3 // Strong
        hasLength -> 2 // Medium
        else -> 1 // Weak
    }

    val passwordsMatch = password.isNotEmpty() && password == confirm
    val isActionEnabled = hasLength && hasSymbol && passwordsMatch && !loading

    // Colors
    val primaryOrange = Color(0xFFFF5722)
    val textGray = Color(0xFF6D6D6D)
    val successGreen = Color(0xFF00C853)
    val inputBg = Color(0xFFF9F9F9)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color(0xFFFFEAD9), // Light Peach Top
                    0.4f to Color.White        // Fade to White
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Header
            Box(Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                    Surface(shape = CircleShape, color = Color.White) {
                        Icon(Icons.Default.ArrowBack, null, modifier = Modifier.padding(8.dp))
                    }
                }
                Text(
                    "Security",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(Modifier.height(30.dp))

            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFF7A18), Color(0xFFFF5722)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LockReset, null, tint = Color.White, modifier = Modifier.size(40.dp))
            }

            Spacer(Modifier.height(16.dp))

            Text("Reset Password", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Your safety is our priority. Please choose a\nstrong new password.",
                textAlign = TextAlign.Center,
                color = textGray,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(30.dp))

            // CARD
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                shadowElevation = 0.dp, // Flat/Subtle shadow handled by container? Figma shows very subtle
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    
                    // New Password
                    Text("New Password", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    
                    CustomPasswordField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "........",
                        icon = Icons.Default.VpnKey,
                        isVisible = passwordVisible,
                        onToggleVisibility = { passwordVisible = !passwordVisible },
                        backgroundColor = inputBg
                    )

                    Spacer(Modifier.height(12.dp))

                    // Strength Bars
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StrengthBar(active = strengthLevel >= 1, color = if (strengthLevel >= 1) successGreen else Color.LightGray)
                        StrengthBar(active = strengthLevel >= 2, color = if (strengthLevel >= 2) successGreen else Color.LightGray)
                        StrengthBar(active = strengthLevel >= 3, color = if (strengthLevel >= 3) successGreen else Color.LightGray)
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        if (strengthLevel == 3) "STRONG PASSWORD" else "WEAK PASSWORD",
                        color = if (strengthLevel == 3) successGreen else Color.Gray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(20.dp))

                    // Confirm Password
                    Text("Confirm Password", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    
                    CustomPasswordField(
                        value = confirm,
                        onValueChange = { confirm = it },
                        placeholder = "........",
                        icon = Icons.Default.VerifiedUser,
                        isVisible = confirmVisible,
                        onToggleVisibility = { confirmVisible = !confirmVisible },
                        backgroundColor = inputBg
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Requirements
            RequirementRow(matched = hasLength, text = "At least 8 characters long")
            Spacer(Modifier.height(8.dp))
            RequirementRow(matched = hasSymbol, text = "Includes a symbol(@#$%)")

            Spacer(Modifier.height(30.dp))

            // Update Button
            Button(
                onClick = { onPasswordUpdated(password) },
                enabled = isActionEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isActionEnabled)
                                Brush.horizontalGradient(listOf(Color(0xFFFF7A18), Color(0xFFFF5722)))
                            else
                                Brush.horizontalGradient(listOf(Color.LightGray, Color.Gray)),
                            RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Update Password", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Footer
            Row {
                Text("Back to ", color = Color.Gray)
                Text(
                    "Login Screen",
                    color = primaryOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
            
            Spacer(Modifier.height(20.dp))
        }
        
        SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun CustomPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    backgroundColor: Color
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(backgroundColor)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = Color(0xFFFF7A18), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Box(Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Color.Gray, fontSize = 16.sp)
                    }
                    innerTextField()
                }
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        null,
                        tint = Color.Gray
                    )
                }
            }
        }
    )
}

@Composable
fun RowScope.StrengthBar(active: Boolean, color: Color) {
    val animatedColor by animateColorAsState(color, label = "color")
    Box(
        modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(animatedColor)
    )
}

@Composable
fun RequirementRow(matched: Boolean, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (matched) Icons.Outlined.CheckCircle else Icons.Outlined.Circle,
            null,
            tint = if (matched) Color(0xFF00C853) else Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(text, color = if (matched) Color.Black else Color.Gray, fontSize = 14.sp)
    }
}
