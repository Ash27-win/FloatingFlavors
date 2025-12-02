//package com.example.floatingflavors.app.feature.auth.presentation.register
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun RegisterScreen(
//    onBackClick: () -> Unit,
//    onRegisterSuccess: () -> Unit
//) {
//    var name by remember { mutableStateOf("") }
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var confirmPassword by remember { mutableStateOf("") }
//    var agreeTerms by remember { mutableStateOf(false) }
//
//    var errorMessage by remember { mutableStateOf<String?>(null) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color(0xFFE8FFD1)) // light green bg
//            .padding(top = 16.dp)
//    ) {
//        // Back arrow
//        IconButton(
//            onClick = onBackClick,
//            modifier = Modifier.align(Alignment.TopStart)
//        ) {
//            Icon(
//                imageVector = Icons.Default.ArrowBack,
//                contentDescription = "Back"
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 24.dp, vertical = 32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Register",
//                fontSize = 26.sp,
//                fontWeight = FontWeight.SemiBold
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            Text(
//                text = "Create your account to order and\nbook catering",
//                fontSize = 13.sp,
//                textAlign = TextAlign.Center
//            )
//
//            Spacer(modifier = Modifier.height(24.dp))
//
//            // White card
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(Color.White)
//                    .padding(16.dp)
//            ) {
//                // Name
//                Text("Name", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
//                Spacer(modifier = Modifier.height(4.dp))
//                OutlinedTextField(
//                    value = name,
//                    onValueChange = { name = it },
//                    modifier = Modifier.fillMaxWidth(),
//                    placeholder = { Text("Enter your name") },
//                    singleLine = true
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Email
//                Text("Email", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
//                Spacer(modifier = Modifier.height(4.dp))
//                OutlinedTextField(
//                    value = email,
//                    onValueChange = { email = it },
//                    modifier = Modifier.fillMaxWidth(),
//                    placeholder = { Text("Enter your email") },
//                    singleLine = true
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Password
//                Text("Password", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
//                Spacer(modifier = Modifier.height(4.dp))
//                OutlinedTextField(
//                    value = password,
//                    onValueChange = { password = it },
//                    modifier = Modifier.fillMaxWidth(),
//                    placeholder = { Text("Enter your password") },
//                    singleLine = true,
//                    visualTransformation = PasswordVisualTransformation()
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Confirm Password
//                Text("Confirm Password", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
//                Spacer(modifier = Modifier.height(4.dp))
//                OutlinedTextField(
//                    value = confirmPassword,
//                    onValueChange = { confirmPassword = it },
//                    modifier = Modifier.fillMaxWidth(),
//                    placeholder = { Text("Enter your confirm password") },
//                    singleLine = true,
//                    visualTransformation = PasswordVisualTransformation()
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Terms checkbox
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Checkbox(
//                        checked = agreeTerms,
//                        onCheckedChange = { agreeTerms = it }
//                    )
//                    Text("I agree to the Terms & Conditions")
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Register button
//                Button(
//                    onClick = {
//                        when {
//                            name.isBlank() || email.isBlank() ||
//                                    password.isBlank() || confirmPassword.isBlank() ->
//                                errorMessage = "Please fill all fields"
//
//                            password != confirmPassword ->
//                                errorMessage = "Passwords do not match"
//
//                            !agreeTerms ->
//                                errorMessage = "Please accept the Terms & Conditions"
//
//                            else -> {
//                                errorMessage = null
//                                // later: save to DB / API
//                                onRegisterSuccess()
//                            }
//                        }
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(44.dp),
//                    shape = RoundedCornerShape(50)
//                ) {
//                    Text("Register")
//                }
//
//                if (errorMessage != null) {
//                    Spacer(modifier = Modifier.height(8.dp))
//                    Text(
//                        text = errorMessage!!,
//                        fontSize = 12.sp,
//                        color = Color.Red
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                // Already account? Login
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Text("Already have an account? ", fontSize = 13.sp)
//                    Text(
//                        text = "Login",
//                        fontSize = 13.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = MaterialTheme.colorScheme.primary,
//                        modifier = Modifier.clickable { onBackClick() }
//                    )
//                }
//            }
//        }
//    }
//}

package com.example.floatingflavors.app.feature.auth.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.floatingflavors.app.feature.auth.presentation.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit,
    onRegisterSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var agreeTerms by remember { mutableStateOf(false) }

    val isLoading = authViewModel.isLoading
    val errorMessage = authViewModel.errorMessage

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8FFD1))
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
                text = "Register",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create your account to order and\nbook catering",
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text("Name", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your name") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Email", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your email") },
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Password", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Confirm Password", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Confirm password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = agreeTerms,
                        onCheckedChange = { agreeTerms = it }
                    )
                    Text("I agree to the Terms & Conditions")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (!agreeTerms) {
                            authViewModel.clearError()
                        } else {
                            authViewModel.register(
                                name = name,
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                role = "User",
                                onSuccess = {
                                    onRegisterSuccess()
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(50),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Register")
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 12.sp,
                        color = Color.Red
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already have an account? ", fontSize = 13.sp)
                    Text(
                        text = "Login",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onBackClick() }
                    )
                }
            }
        }
    }
}
