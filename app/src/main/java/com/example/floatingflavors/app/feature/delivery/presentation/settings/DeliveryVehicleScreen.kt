package com.example.floatingflavors.app.feature.delivery.presentation.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.platform.testTag
import com.example.floatingflavors.app.core.util.TestTags
import com.example.floatingflavors.app.feature.delivery.presentation.components.SectionHeader
import com.example.floatingflavors.app.feature.delivery.presentation.components.StatusBadge
import com.example.floatingflavors.app.feature.delivery.presentation.components.DeliveryTopBar
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryVehicleDto
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border

enum class ComplianceStatus { ACTIVE, EXPIRING_SOON, EXPIRED }

fun calculateCompliance(dateString: String): ComplianceStatus {
    if (dateString.isEmpty()) return ComplianceStatus.EXPIRED
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        val expiryDate = LocalDate.parse(dateString, formatter)
        val daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate)
        when {
            daysBetween < 0 -> ComplianceStatus.EXPIRED
            daysBetween <= 30 -> ComplianceStatus.EXPIRING_SOON
            else -> ComplianceStatus.ACTIVE
        }
    } catch (e: Exception) {
        ComplianceStatus.EXPIRED
    }
}

// Helper for Date Formatting
fun formatVehicleDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "Not Available"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryVehicleScreen(
    viewModel: DeliveryVehicleViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showImageViewer by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.uploadVehicleImage(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }
    
    // Automatically refresh data when returning to this screen
    LaunchedEffect(Unit) {
        viewModel.loadVehicleInfo()
    }

    // In-App Image Viewer Dialog
    if (showImageViewer) {
        val vehicle = (uiState as? DeliveryVehicleUiState.Success)?.vehicleInfo
        Dialog(
            onDismissRequest = { showImageViewer = false },
            properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true, dismissOnClickOutside = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (!vehicle?.vehicleImage.isNullOrEmpty()) {
                    val cacheBuster = remember(vehicle?.vehicleImage) { System.currentTimeMillis() }
                    SubcomposeAsyncImage(
                        model = "${vehicle?.vehicleImage}?t=$cacheBuster",
                        contentDescription = "Full Screen Vehicle Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                        loading = {
                            CircularProgressIndicator(color = Color(0xFFE65100), modifier = Modifier.size(48.dp))
                        },
                        error = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.BrokenImage, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Failed to load image", color = Color.White)
                            }
                        }
                    )
                }
                
                // Close Button
                IconButton(
                    onClick = { showImageViewer = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.imePadding(),
        topBar = {
            DeliveryTopBar(title = "VEHICLE INFORMATION", onBack = onBack)
        },
        containerColor = Color(0xFFFFFBF7),
        bottomBar = {
            val vehicle = (uiState as? DeliveryVehicleUiState.Success)?.vehicleInfo
            val hasImage = !vehicle?.vehicleImage.isNullOrEmpty()

            Surface(
                color = Color.White,
                shadowElevation = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (hasImage) "STATUS: DOCUMENT AVAILABLE" else "STATUS: NO IMAGE UPLOADED",
                        fontSize = 11.sp,
                        color = if (hasImage) Color(0xFF388E3C) else Color(0xFFE65100),
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // VIEW BUTTON (Enabled only if image exists)
                        OutlinedButton(
                            onClick = { 
                                showImageViewer = true
                            },
                            enabled = hasImage && !isUploading,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(50),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(if (hasImage) Color(0xFFE65100) else Color.LightGray))
                        ) {
                            Text("VIEW", color = if (hasImage) Color(0xFFE65100) else Color.LightGray, fontWeight = FontWeight.Bold)
                        }

                        // UPLOAD/REPLACE BUTTON
                        Button(
                            onClick = { launcher.launch("image/*") },
                            enabled = !isUploading,
                            modifier = Modifier.weight(1.5f).height(56.dp),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                        ) {
                            if (isUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text(if (hasImage) "REPLACE" else "UPLOAD", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(8.dp))
                                Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is DeliveryVehicleUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE65100))
                }
            }
            is DeliveryVehicleUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Upload failed. Try again.", color = Color.Red, fontWeight = FontWeight.Bold)
                    android.util.Log.e("DeliveryVehicle", "Error: ${state.message}")
                }
            }
            is DeliveryVehicleUiState.Success -> {
                val vehicle = state.vehicleInfo

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TestTags.DELIVERY_VEHICLE_SCREEN)
                        .padding(padding)
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Top Image
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!vehicle.vehicleImage.isNullOrEmpty()) {
                                val cacheBuster = remember(vehicle.vehicleImage) { System.currentTimeMillis() }
                                SubcomposeAsyncImage(
                                    model = "${vehicle.vehicleImage}?t=$cacheBuster",
                                    contentDescription = "Vehicle Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    loading = {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFE65100))
                                        }
                                    },
                                    error = {
                                        Icon(Icons.Default.Error, null, tint = Color.Red, modifier = Modifier.size(40.dp))
                                    }
                                )
                            } else {
                                // Fallback Layout Matching Figma
                                Box(
                                    modifier = Modifier.fillMaxSize().background(Color(0xFFFFF3E0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.DirectionsBike, null, tint = Color(0xFFFFB74D), modifier = Modifier.size(56.dp))
                                        Spacer(Modifier.height(12.dp))
                                        Text("No Vehicle Photo", color = Color(0xFFE65100), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("Tap the camera below to upload", color = Color(0xFFFFB74D), fontSize = 12.sp)
                                    }
                                }
                            }
                            
                            // Edit overlay
                            IconButton(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .background(Color.White, CircleShape)
                                    .size(44.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color(0xFFE65100), modifier = Modifier.size(22.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(30.dp))

                    SectionHeader("VEHICLE DETAILS")

                    Spacer(Modifier.height(16.dp))

                    // Form Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(vertical = 12.dp)) {
                            val vType by viewModel.editVehicleType.collectAsState()
                            val vModel by viewModel.editModelName.collectAsState()
                            val vNumber by viewModel.editVehicleNumber.collectAsState()
                            val vYear by viewModel.editRegistrationYear.collectAsState()

                            EditableVehicleDetailItem(
                                icon = Icons.Default.DirectionsBike,
                                label = "VEHICLE TYPE",
                                value = vType,
                                onValueChange = { viewModel.editVehicleType.value = it },
                                iconBg = Color(0xFFFFF3E0),
                                iconTint = Color(0xFFE65100),
                                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            EditableVehicleDetailItem(
                                icon = Icons.Default.BrandingWatermark,
                                label = "MODEL NAME",
                                value = vModel,
                                onValueChange = { viewModel.editModelName.value = it },
                                iconBg = Color(0xFFE3F2FD),
                                iconTint = Color(0xFF1976D2),
                                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            EditableVehicleDetailItem(
                                icon = Icons.Default.Badge,
                                label = "VEHICLE NUMBER",
                                value = vNumber,
                                onValueChange = { viewModel.editVehicleNumber.value = it },
                                iconBg = Color(0xFFF5F5F5),
                                iconTint = Color(0xFF424242),
                                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            EditableVehicleDetailItem(
                                icon = Icons.Default.DateRange,
                                label = "REGISTRATION YEAR",
                                value = vYear,
                                onValueChange = { viewModel.editRegistrationYear.value = it },
                                iconBg = Color(0xFFF3E5F5),
                                iconTint = Color(0xFF7B1FA2),
                                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Save Button
                    Button(
                        onClick = { 
                            focusManager.clearFocus()
                            viewModel.saveVehicleInfo() 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                    ) {
                        Text("SAVE VEHICLE DETAILS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                    }

                    Spacer(Modifier.height(30.dp))

                    SectionHeader("COMPLIANCE")

                    Spacer(Modifier.height(16.dp))

                    // Compliance Card
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(vertical = 12.dp)) {
                            val vInsurance by viewModel.editInsuranceExpiry.collectAsState()
                            
                            EditableVehicleDetailItem(
                                icon = Icons.Default.Security,
                                label = "INSURANCE EXPIRY",
                                value = vInsurance,
                                onValueChange = { viewModel.editInsuranceExpiry.value = it },
                                iconBg = Color(0xFFE8F5E9),
                                iconTint = Color(0xFF2E7D32),
                                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { 
                                    focusManager.clearFocus()
                                    viewModel.saveVehicleInfo()
                                })
                            )
                            
                            // Animated Dynamic Compliance Badge
                            val status = calculateCompliance(vInsurance)
                            
                            val bgColor by animateColorAsState(
                                targetValue = when (status) {
                                    ComplianceStatus.ACTIVE -> Color(0xFFE8F5E9)
                                    ComplianceStatus.EXPIRING_SOON -> Color(0xFFFFF3E0)
                                    ComplianceStatus.EXPIRED -> Color(0xFFFFEBEE)
                                },
                                animationSpec = tween(500)
                            )
                            val fgColor by animateColorAsState(
                                targetValue = when (status) {
                                    ComplianceStatus.ACTIVE -> Color(0xFF388E3C)
                                    ComplianceStatus.EXPIRING_SOON -> Color(0xFFF57C00)
                                    ComplianceStatus.EXPIRED -> Color(0xFFD32F2F)
                                },
                                animationSpec = tween(500)
                            )
                            val iconVector = when (status) {
                                ComplianceStatus.ACTIVE -> Icons.Default.VerifiedUser
                                ComplianceStatus.EXPIRING_SOON -> Icons.Default.Warning
                                ComplianceStatus.EXPIRED -> Icons.Default.Error
                            }
                            val textLabel = when (status) {
                                ComplianceStatus.ACTIVE -> "Compliant - Active"
                                ComplianceStatus.EXPIRING_SOON -> "Expiring Soon - Update Required"
                                ComplianceStatus.EXPIRED -> "EXPIRED - CRITICAL ACTION REQUIRED"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp)
                                    .background(bgColor, RoundedCornerShape(12.dp))
                                    .border(if (status == ComplianceStatus.EXPIRED) 1.dp else 0.dp, fgColor, RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(iconVector, contentDescription = null, tint = fgColor, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(textLabel, color = fgColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Bottom Navigation Spacer
                    Spacer(Modifier.height(120.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableVehicleDetailItem(
    icon: ImageVector, 
    label: String, 
    value: String, 
    onValueChange: (String) -> Unit,
    iconBg: Color, 
    iconTint: Color,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(iconBg, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color(0xFFE65100).copy(alpha = 0.5f),
                    unfocusedIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black),
                singleLine = true,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions
            )
        }
        if (value.isNotBlank()) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
        }
    }
}

