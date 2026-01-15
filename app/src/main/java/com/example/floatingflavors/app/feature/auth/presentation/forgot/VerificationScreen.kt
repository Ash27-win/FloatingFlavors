package com.example.floatingflavors.app.feature.auth.presentation.forgot

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var otp by remember { mutableStateOf(List(6) { "" }) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let { snackbarHostState.showSnackbar(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Color(0xFFFFEFE3), Color.White)
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            IconButton(onClick = onBack) {
                Surface(shape = CircleShape, color = Color.White) {
                    Icon(Icons.Default.ArrowBack, null, modifier = Modifier.padding(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
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

                    Spacer(Modifier.height(8.dp))

                    Text(
                        "Enter the 6-digit code sent to\n$emailMasked",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        otp.forEachIndexed { index, value ->
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .border(
                                        2.dp,
                                        if (value.isNotEmpty()) Color(0xFFFF7A18) else Color.LightGray,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicTextField(
                                    value = value,
                                    onValueChange = {
                                        if (it.length <= 1 && it.all(Char::isDigit)) {
                                            otp = otp.toMutableList().also { list ->
                                                list[index] = it
                                            }
                                        }
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFFF3EA)
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

                    val resendColor by animateColorAsState(
                        if (seconds == 0) Color(0xFFFF7A18) else Color.Gray,
                        label = "resend"
                    )

                    TextButton(
                        enabled = seconds == 0 && !loading,
                        onClick = onResend
                    ) {
                        Text("Resend", color = resendColor)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = { onVerify(otp.joinToString("")) },
                enabled = otp.all { it.isNotEmpty() } && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A18))
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text("Verify & Proceed →", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
