package com.example.floatingflavors.app.feature.auth.presentation.forgot

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun VerificationScreen(
    emailMasked: String,
    seconds: Int,
    loading: Boolean,
    message: String?,
    onBack: () -> Unit,
    onVerify: (String) -> Unit,
    onResend: () -> Unit
) {
    val otpLength = 6
    var otp by remember { mutableStateOf(List(otpLength) { "" }) }
    val focusRequesters = remember { List(otpLength) { FocusRequester() } }
    val snackbarHostState = remember { SnackbarHostState() }

    // 👇 Auto focus first box
    LaunchedEffect(Unit) {
        focusRequesters.first().requestFocus()
    }

    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFFFF1E6), Color.White)
                )
            )
            .padding(20.dp)
    ) {

        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, null)
                }
                Text(
                    "Verification",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            // Card
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color(0xFFFFF8F3),
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Icon(
                        Icons.Default.MarkEmailRead,
                        null,
                        tint = Color(0xFFFF7A18),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text("Verify Identity", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Enter the 6-digit code sent to your email",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        emailMasked,
                        color = Color(0xFFFF7A18),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(24.dp))

                    // OTP INPUT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        otp.forEachIndexed { index, value ->
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .border(
                                        2.dp,
                                        if (value.isNotEmpty())
                                            Color(0xFFFF7A18)
                                        else
                                            Color(0xFFE0E0E0),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicTextField(
                                    value = value,
                                    onValueChange = { input ->
                                        when {
                                            // 👇 Paste full OTP
                                            input.length == otpLength && input.all(Char::isDigit) -> {
                                                otp = input.map { it.toString() }
                                            }

                                            input.length <= 1 && input.all(Char::isDigit) -> {
                                                otp = otp.toMutableList().also {
                                                    it[index] = input
                                                }
                                                if (input.isNotEmpty() && index < otpLength - 1) {
                                                    focusRequesters[index + 1].requestFocus()
                                                }
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .focusRequester(focusRequesters[index])
                                        .onKeyEvent { event ->
                                            val isBackspace = event.key == Key.Backspace
                                            val isKeyDown = event.nativeKeyEvent.action == android.view.KeyEvent.ACTION_DOWN
                                            if (isBackspace && isKeyDown && value.isEmpty() && index > 0) {
                                                focusRequesters[index - 1].requestFocus()
                                                true
                                            } else {
                                                false
                                            }
                                        },
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Timer
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFFEFE3)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, null, tint = Color(0xFFFF7A18))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                String.format("00:%02d", seconds),
                                color = Color(0xFFFF7A18),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row {
                        Text("Didn't receive code? ")
                        Text(
                            "Resend",
                            color = if (seconds == 0) Color(0xFFFF7A18) else Color.LightGray,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(enabled = seconds == 0) {
                                onResend()
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // Verify button
            Button(
                onClick = { onVerify(otp.joinToString("")) },
                enabled = otp.all { it.isNotEmpty() } && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                if (otp.all { it.isNotEmpty() } && !loading)
                                    listOf(Color(0xFFFF7A18), Color(0xFFFF9A3C))
                                else
                                    listOf(Color.Gray, Color.LightGray)
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
                            "Verify & Proceed →",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}