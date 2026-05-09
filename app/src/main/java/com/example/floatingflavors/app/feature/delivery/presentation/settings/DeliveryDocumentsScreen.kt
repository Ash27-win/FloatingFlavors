package com.example.floatingflavors.app.feature.delivery.presentation.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.floatingflavors.app.core.util.TestTags
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryDocumentDto
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDocumentsScreen(
    viewModel: DeliveryDocumentsViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var activeDoc by remember { mutableStateOf<DeliveryDocumentDto?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let {
            activeDoc?.let { doc ->
                viewModel.uploadDocument(it, doc.type ?: "general")
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally, 
                        modifier = Modifier.fillMaxWidth().padding(end = 48.dp)
                    ) {
                        Text(
                            text = "Documents",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1A202C)
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "ENTERPRISE VAULT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA0AEC0),
                            letterSpacing = 1.2.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Back", modifier = Modifier.padding(8.dp), tint = Color(0xFF4A5568))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF8F9FA))
            )
        },
        containerColor = Color(0xFFF8F9FA),
        bottomBar = {
            // Footer
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "FLOATING FLAVORS",
                    fontSize = 12.sp,
                    color = Color(0xFFA0AEC0),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFA0AEC0), modifier = Modifier.size(10.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "END-TO-END ENCRYPTED",
                        fontSize = 9.sp,
                        color = Color(0xFFA0AEC0),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is DeliveryDocumentsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFE65100))
                }
            }
            is DeliveryDocumentsUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Upload failed. Try again.", color = Color.Red, fontWeight = FontWeight.Bold)
                    android.util.Log.e("DeliveryDocs", "Error: ${state.message}")
                }
            }
            is DeliveryDocumentsUiState.Success -> {
                val requiredDocs = state.documents.filter { doc ->
                    val t = doc.type?.lowercase() ?: ""
                    t.contains("license") || t.contains("aadhaar")
                }
                val vehicleDocs = state.documents.filter { doc ->
                    val t = doc.type?.lowercase() ?: ""
                    t.contains("vehicle") || t.contains("insurance") || t.contains("rc")
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TestTags.DELIVERY_DOCUMENTS_SCREEN)
                        .padding(padding),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    if (requiredDocs.isNotEmpty()) {
                        item {
                            ModernSectionHeader("IDENTITY VERIFICATION")
                            Spacer(Modifier.height(16.dp))
                        }
                        items(requiredDocs) { doc ->
                            NewDocumentCard(
                                doc = doc, 
                                isUploading = isUploading && activeDoc?.type == doc.type,
                                onUpdate = {
                                    activeDoc = doc
                                    launcher.launch("*/*")
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }

                    if (vehicleDocs.isNotEmpty()) {
                        item {
                            ModernSectionHeader("FLEET COMPLIANCE")
                            Spacer(Modifier.height(16.dp))
                        }
                        items(vehicleDocs) { doc ->
                            NewDocumentCard(
                                doc = doc, 
                                isUploading = isUploading && activeDoc?.type == doc.type,
                                onUpdate = {
                                    activeDoc = doc
                                    launcher.launch("*/*")
                                }
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(84.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ModernSectionHeader(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFA0AEC0),
            letterSpacing = 1.sp
        )
        Spacer(Modifier.width(16.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFF0F2F5), thickness = 2.dp)
    }
}

@Composable
fun NewDocumentCard(
    doc: DeliveryDocumentDto, 
    isUploading: Boolean,
    onUpdate: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val docType = doc.type?.lowercase() ?: ""
    val titleSafe = when {
        docType.contains("license") -> "Driver's License"
        docType.contains("aadhaar") -> "Aadhar Card / ID"
        docType.contains("rc") || docType.contains("registration") -> "Registration (RC)"
        docType.contains("insurance") -> "Insurance Policy"
        else -> doc.type?.uppercase() ?: "DOCUMENT"
    }
    
    val _icon = when {
        docType.contains("insurance") -> Icons.Default.GppMaybe // Shield with warning
        docType.contains("license") -> Icons.Default.Badge
        docType.contains("aadhaar") -> Icons.Default.AssignmentInd
        docType.contains("rc") || docType.contains("vehicle") -> Icons.Default.Description
        else -> Icons.Default.InsertDriveFile
    }

    val statusText = doc.status?.lowercase() ?: "pending"
    
    // Exact Custom Mapping based on Figma requirements
    val (statusLabel, statusBg, statusColor) = when (statusText) {
        "verified" -> Triple("VERIFIED", Color(0xFFE6FFFA), Color(0xFF38A169))
        "pending" -> Triple("IN REVIEW", Color(0xFFFFFFF0), Color(0xFFD69E2E))
        else -> Triple("ACTION\nREQUIRED", Color(0xFFFFF5F5), Color(0xFFE53E3E))
    }

    // Determine icon colors based on status
    val isNegativeStatus = statusText in listOf("rejected", "expired", "not_uploaded")
    val iconBgColor = if (isNegativeStatus) Color(0xFFFFF0E6) else Color(0xFFF4F6F8)
    val iconTintColor = if (isNegativeStatus) Color(0xFFE65100) else Color(0xFF4A5568)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBgColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = iconTintColor, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = _icon,
                        contentDescription = null,
                        tint = iconTintColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Title and Status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleSafe,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
                
                if (statusText != "not_uploaded") {
                    Spacer(Modifier.height(6.dp))
                    // Status Pill
                    Row(
                        modifier = Modifier
                            .background(statusBg, CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small dot
                        Canvas(modifier = Modifier.size(4.dp)) {
                            drawCircle(color = statusColor)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Start,
                            lineHeight = 11.sp
                        )
                    }
                }
            }

            Spacer(Modifier.width(4.dp))

            // Actions View/Update/Upload
            val hasDocument = !doc.documentUrl.isNullOrEmpty()
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hasDocument) {
                    Text(
                        text = "View",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF38B2AC),
                        modifier = Modifier
                            .clickable {
                                doc.documentUrl?.let { url ->
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        setDataAndType(android.net.Uri.parse(url), "*/*")
                                        flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    context.startActivity(intent)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
                
                Text(
                    text = if (hasDocument) "Update" else "Upload",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFED8936),
                    modifier = Modifier
                        .clickable(enabled = !isUploading) { onUpdate() }
                        .padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
                )
            }
        }
    }
}
