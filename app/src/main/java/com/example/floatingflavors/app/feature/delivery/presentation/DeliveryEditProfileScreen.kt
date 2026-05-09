package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.floatingflavors.app.feature.delivery.presentation.profile.DeliveryProfileViewModel
import com.example.floatingflavors.app.feature.delivery.presentation.profile.DeliveryProfileUiState

import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import com.example.floatingflavors.app.feature.delivery.presentation.components.DeliveryTopBar
import androidx.compose.foundation.text.KeyboardActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryEditProfileScreen(
    viewModel: DeliveryProfileViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Form States
    val name by viewModel.editName.collectAsState()
    val email by viewModel.editEmail.collectAsState()
    val phone by viewModel.editPhone.collectAsState()
    val emergency by viewModel.editEmergency.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val imageUri by viewModel.editImageUri.collectAsState()
    val profile = (uiState as? DeliveryProfileUiState.Success)?.profile
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    val focusManager = LocalFocusManager.current

    // Gallery Launcher
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.editImageUri.value = it }
    }

    // Camera Launcher
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            viewModel.editImageUri.value = tempCameraUri
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            DeliveryTopBar(title = "EDIT PROFILE", onBack = onBack)
        },
        containerColor = Color(0xFFFFFBF7),
        modifier = Modifier.imePadding()
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Avatar Edit
            Box(contentAlignment = Alignment.BottomEnd) {
                // Image
                if (imageUri != null) {
                    AsyncImage(model = imageUri, contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape), contentScale = ContentScale.Crop)
                } else {
                     AsyncImage(model = profile?.profileImage, contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape).border(4.dp, Color.White, CircleShape), contentScale = ContentScale.Crop)
                }

                // Camera Icon
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(36.dp)
                        .background(Color(0xFFFF6D00), CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                        .clickable { showImageSourceDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            // Image Source Dialog
            if (showImageSourceDialog) {
                AlertDialog(
                    onDismissRequest = { showImageSourceDialog = false },
                    title = { Text("Change Profile Photo") },
                    text = { Text("Choose an option to update your profile picture.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        }) {
                            Text("Gallery")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showImageSourceDialog = false
                            // Create temp file for camera
                            val file = java.io.File(context.cacheDir, "temp_camera_img_${System.currentTimeMillis()}.jpg")
                            val uri = androidx.core.content.FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            tempCameraUri = uri
                            cameraLauncher.launch(uri)
                        }) {
                            Text("Camera")
                        }
                    }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Form Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(24.dp)) {
                    
                    EditField(
                        label = "FULL NAME", 
                        value = name, 
                        onValueChange = { viewModel.editName.value = it }, 
                        icon = Icons.Default.Person,
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    Spacer(Modifier.height(20.dp))
                    
                    EditField(
                        label = "PHONE NUMBER", 
                        value = phone, 
                        onValueChange = { viewModel.editPhone.value = it }, 
                        icon = Icons.Default.Phone, 
                        isLocked = true,
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, start = 4.dp)) {
                        Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Verify your phone number is correct for delivery updates.", fontSize = 10.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.height(20.dp))
                    
                    EditField(
                        label = "EMAIL ADDRESS", 
                        value = email, 
                        onValueChange = { viewModel.editEmail.value = it }, 
                        icon = Icons.Default.Email,
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    Spacer(Modifier.height(20.dp))

                    EditField(
                        label = "EMERGENCY CONTACT", 
                        value = emergency, 
                        onValueChange = { viewModel.editEmergency.value = it }, 
                        icon = Icons.Default.ContactPhone,
                        keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { 
                            focusManager.clearFocus()
                            viewModel.updateProfile { onBack() }
                        })
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = { 
                    focusManager.clearFocus()
                    viewModel.updateProfile { onBack() } 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6D00))
            ) {
                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
            }
            
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("SECURE PROFILE MANAGEMENT", fontSize = 10.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditField(
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit, 
    icon: ImageVector, 
    isLocked: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFCCBC), letterSpacing = 1.sp)
        Spacer(Modifier.height(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = if (isLocked) Color(0xFFFAFAFA) else Color(0xFFF9F9F9),
                unfocusedContainerColor = if (isLocked) Color(0xFFFAFAFA) else Color(0xFFF9F9F9),
                disabledContainerColor = if (isLocked) Color(0xFFFAFAFA) else Color(0xFFF9F9F9),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(12.dp),
            leadingIcon = {
                Icon(icon, null, tint = Color.Gray)
            },
            trailingIcon = {
                if(isLocked) {
                    Icon(Icons.Default.Lock, null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                } else if (value.isNotBlank()) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                }
            },
            enabled = !isLocked,
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }
}
