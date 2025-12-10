package com.example.floatingflavors.app.feature.admin.presentation.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.admin.data.remote.dto.AdminSettingsDto
import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminPrefRequest
import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminSettingsRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.InputStream

@Composable
fun AdminSettingsScreen(
    viewModel: AdminSettingsViewModel,
    onSignOut: (() -> Unit)? = null
) {
    // UI state (null while loading)
    val uiState by viewModel.state.collectAsState(initial = null)

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // compute status bar padding (so header can extend behind it)
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Image picker
    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                uriToMultipart(context.contentResolver, uri, "avatar")?.let { part ->
                    viewModel.uploadAvatar(uiState?.admin_id ?: 1, part)
                }
            }
        }
    }

    // load on first composition
    LaunchedEffect(Unit) {
        if (uiState == null) viewModel.load(adminId = 1)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        // --- IMPORTANT: drop the top padding from Scaffold innerPadding to avoid top white strip ---
        val layoutDirection = LocalLayoutDirection.current
        val startPad = innerPadding.calculateStartPadding(layoutDirection)
        val endPad = innerPadding.calculateEndPadding(layoutDirection)
        val bottomPad = innerPadding.calculateBottomPadding()
        // (intentionally ignore innerPadding.calculateTopPadding())

        // loader
        if (uiState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // apply same horizontal + bottom paddings, but top left as 0 so header sits at screen top
                    .padding(start = startPad, end = endPad, bottom = bottomPad)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // local state for edit dialog
        var showEditDialog by remember { mutableStateOf(false) }

        // root column WITHOUT global horizontal padding so header is edge-to-edge.
        Column(
            modifier = Modifier
                .fillMaxSize()
                // apply only horizontal + bottom padding from scaffold; intentionally skip top padding
                .padding(start = startPad, end = endPad, bottom = bottomPad)
                .verticalScroll(scrollState)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header - extend into status bar by adding the inset to height, but pad the content down
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    // Add status bar height so background covers the area behind status bar icons
                    .height(160.dp + statusBarPadding)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                androidx.compose.ui.graphics.Color(0xFF4F46E5),
                                androidx.compose.ui.graphics.Color(0xFF7C3AED)
                            )
                        ),
                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                    // internal padding: top must be statusBarPadding so header content sits below system icons
                    .padding(start = 20.dp, top = statusBarPadding + 12.dp, end = 20.dp, bottom = 12.dp)
            ) {
                Column {
                    Text(
                        text = "Admin Profile",
                        style = MaterialTheme.typography.headlineMedium.copy(color = androidx.compose.ui.graphics.Color.White),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Manage your account settings",
                        style = MaterialTheme.typography.bodyMedium.copy(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.95f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Profile card - keep horizontal padding here (so card is inset)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .offset(y = (-48).dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color(0xFFF3EFF6))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 22.dp, horizontal = 20.dp)
                    ) {
                        // Container for avatar + camera icon
                        Box(
                            modifier = Modifier
                                .size(140.dp), // container size for avatar + camera
                            contentAlignment = Alignment.Center
                        ) {
                            // build avatar url (handles relative or absolute)
                            val raw = uiState!!.avatar_url
                            val avatarUrl = raw?.takeIf { it.isNotBlank() }?.let {
                                if (it.startsWith("http", ignoreCase = true)) it else NetworkClient.BASE_URL + it
                            }

                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(avatarUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Admin avatar",
                                    modifier = Modifier
                                        .size(130.dp) // avatar size increased slightly
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = uiState!!.full_name
                                        ?.trim()
                                        ?.split("\\s+".toRegex())
                                        ?.mapNotNull { it.firstOrNull()?.toString() }
                                        ?.take(2)
                                        ?.joinToString("") ?: ""
                                    Text(text = initials, style = MaterialTheme.typography.titleMedium)
                                }
                            }

                            // Camera button: smaller and anchored bottom-end INSIDE the avatar container.
                            IconButton(
                                onClick = { pickLauncher.launch("image/*") },
                                modifier = Modifier
                                    .size(38.dp) // smaller outer circle
                                    .align(Alignment.BottomEnd)
                                    // negative offset to move slightly inside the avatar edge for nice overlap
                                    .offset(x = (-6).dp, y = (-6).dp)
                                    .clip(CircleShape)
                                    .background(androidx.compose.ui.graphics.Color(0xFF10B981))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change avatar",
                                    tint = androidx.compose.ui.graphics.Color.White,
                                    modifier = Modifier.size(20.dp) // smaller icon inside
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(text = uiState!!.full_name ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(text = uiState!!.email ?: "", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(shape = RoundedCornerShape(50), color = androidx.compose.ui.graphics.Color(0xFFE6F4EA)) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Admin", style = MaterialTheme.typography.labelSmall.copy(color = androidx.compose.ui.graphics.Color(0xFF1F8A3A)))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showEditDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .height(48.dp),
                            shape = RoundedCornerShape(40.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF10B981))
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Edit Profile", color = androidx.compose.ui.graphics.Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Account Details card with local horizontal padding
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Account Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Your personal and business information", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow(icon = Icons.Default.Person, label = "Full Name", value = uiState!!.full_name ?: "")
                    InfoRow(icon = Icons.Outlined.MailOutline, label = "Email Address", value = uiState!!.email ?: "")
                    InfoRow(icon = Icons.Outlined.Phone, label = "Phone Number", value = uiState!!.phone ?: "")
                    InfoRow(icon = Icons.Default.Domain, label = "Business Name", value = uiState!!.business_name ?: "")
                    InfoRow(icon = Icons.Outlined.LocationOn, label = "Kitchen Address", value = uiState!!.address ?: "")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notification Prefs (same inset pattern)
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Notification Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Manage what notifications you receive", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    Spacer(modifier = Modifier.height(12.dp))

                    val newOrder = uiState!!.new_order_alert ?: false
                    val lowStock = uiState!!.low_stock_alert ?: false
                    val aiInsights = uiState!!.ai_insights ?: false
                    val feedback = uiState!!.customer_feedback ?: false

                    ToggleRow(label = "New Order Alerts", description = "Get notified for new orders", checked = newOrder) { checked ->
                        viewModel.updatePreferences(UpdateAdminPrefRequest(admin_id = uiState!!.admin_id ?: 0, new_order_alerts = if (checked) 1 else 0, low_stock_alerts = if (lowStock) 1 else 0, ai_insights = if (aiInsights) 1 else 0, customer_feedback = if (feedback) 1 else 0))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ToggleRow(label = "Low Stock Alerts", description = "Alerts when inventory is low", checked = lowStock) { checked ->
                        viewModel.updatePreferences(UpdateAdminPrefRequest(admin_id = uiState!!.admin_id ?: 0, new_order_alerts = if (newOrder) 1 else 0, low_stock_alerts = if (checked) 1 else 0, ai_insights = if (aiInsights) 1 else 0, customer_feedback = if (feedback) 1 else 0))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ToggleRow(label = "AI Insights", description = "Receive AI-powered tips", checked = aiInsights) { checked ->
                        viewModel.updatePreferences(UpdateAdminPrefRequest(admin_id = uiState!!.admin_id ?: 0, new_order_alerts = if (newOrder) 1 else 0, low_stock_alerts = if (lowStock) 1 else 0, ai_insights = if (checked) 1 else 0, customer_feedback = if (feedback) 1 else 0))
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    ToggleRow(label = "Customer Feedback", description = "New reviews and ratings", checked = feedback) { checked ->
                        viewModel.updatePreferences(UpdateAdminPrefRequest(admin_id = uiState!!.admin_id ?: 0, new_order_alerts = if (newOrder) 1 else 0, low_stock_alerts = if (lowStock) 1 else 0, ai_insights = if (aiInsights) 1 else 0, customer_feedback = if (checked) 1 else 0))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Security + Signout
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Security")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { /* change password */ }, modifier = Modifier.fillMaxWidth()) { Text("Change Password") }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = { /* 2FA */ }, modifier = Modifier.fillMaxWidth()) { Text("Two-Factor Authentication") }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { onSignOut?.invoke() },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFDC2626))
            ) {
                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = androidx.compose.ui.graphics.Color.White)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Edit dialog (modal)
        if (showEditDialog) {
            EditProfileDialog(
                initial = uiState!!,
                onDismiss = { showEditDialog = false },
                onSave = { req ->
                    viewModel.updateProfile(req)
                    showEditDialog = false
                }
            )
        }
    }
}

/** Helpers below are identical to previous version (InfoRow, ToggleRow, EditProfileDialog, uriToMultipart) **/

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(44.dp), tonalElevation = 1.dp) {
            Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ToggleRow(label: String, description: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = description, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}

@Composable
private fun EditProfileDialog(initial: AdminSettingsDto, onDismiss: () -> Unit, onSave: (UpdateAdminSettingsRequest) -> Unit) {
    var name by remember { mutableStateOf(initial.full_name ?: "") }
    var email by remember { mutableStateOf(initial.email ?: "") }
    var phone by remember { mutableStateOf(initial.phone ?: "") }
    var business by remember { mutableStateOf(initial.business_name ?: "") }
    var address by remember { mutableStateOf(initial.address ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onSave(UpdateAdminSettingsRequest(admin_id = initial.admin_id ?: 0, full_name = name, email = email, phone = phone, business_name = business, address = address)) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Edit Profile") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = business, onValueChange = { business = it }, label = { Text("Business Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        }
    )
}

/** Utility: convert Uri -> MultipartBody.Part **/
suspend fun uriToMultipart(contentResolver: android.content.ContentResolver, uri: Uri, fieldName: String): MultipartBody.Part? {
    return try {
        val input: InputStream? = contentResolver.openInputStream(uri)
        val bytes = input?.readBytes() ?: return null
        input.close()
        val filename = uri.lastPathSegment?.substringAfterLast('/') ?: "avatar.jpg"
        val requestBody: RequestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        MultipartBody.Part.createFormData(fieldName, filename, requestBody)
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }
}






//package com.example.floatingflavors.app.feature.admin.presentation.settings
//
//import android.net.Uri
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.LocationOn
//import androidx.compose.material.icons.outlined.MailOutline
//import androidx.compose.material.icons.outlined.Phone
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
//import coil.request.ImageRequest
//import com.example.floatingflavors.app.core.network.NetworkClient
//import com.example.floatingflavors.app.feature.admin.data.remote.dto.AdminSettingsDto
//import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminPrefRequest
//import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminSettingsRequest
//import kotlinx.coroutines.launch
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.toRequestBody
//import java.io.InputStream
//
///**
// * Admin Settings Screen
// *
// * Matches Figma: purple header, floating profile card, account details card,
// * notification prefs card, security card, sign out button.
// *
// * Uses Material3 components and SnackbarHostState (no deprecated scaffoldState).
// *
// * Replace or adapt small details to match exact theme/colors in your app.
// */
//@Composable
//fun AdminSettingsScreen(
//    viewModel: AdminSettingsViewModel,
//    onSignOut: (() -> Unit)? = null
//) {
//    // collect state safely with initial null
//    val uiState by viewModel.state.collectAsState(initial = null)
//
//    val snackbarHostState = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()
//    val context = LocalContext.current
//    val scrollState = rememberScrollState()
//
//    // Image picker
//    val pickLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        if (uri != null) {
//            scope.launch {
//                uriToMultipart(context.contentResolver, uri, "avatar")?.let { part ->
//                    viewModel.uploadAvatar(uiState?.admin_id ?: 1, part)
//                }
//            }
//        }
//    }
//
//    // optional events flow: show snackbars from VM (if VM exposes events SharedFlow<String>)
//    LaunchedEffect(viewModel) {
//        // If your VM has `events: SharedFlow<String>` uncomment the next lines
//        // viewModel.events.collect { message ->
//        //     snackbarHostState.showSnackbar(message)
//        // }
//    }
//
//    // initial load
//    LaunchedEffect(Unit) {
//        if (uiState == null) viewModel.load(adminId = 1) // change adminId if dynamic
//    }
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) }
//    ) { innerPadding ->
//
//        // Loading state
//        if (uiState == null) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(innerPadding)
//                    .background(MaterialTheme.colorScheme.background),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//            return@Scaffold
//        }
//
//        // local dialog state
//        var showEditDialog by remember { mutableStateOf(false) }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .verticalScroll(scrollState)
//                .background(MaterialTheme.colorScheme.background)
//                .padding(innerPadding)
//                .padding(horizontal = 16.dp)
//        ) {
//            // Header gradient
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(160.dp)
//                    .background(
//                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
//                            colors = listOf(
//                                androidx.compose.ui.graphics.Color(0xFF4F46E5),
//                                androidx.compose.ui.graphics.Color(0xFF7C3AED)
//                            )
//                        ),
//                        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
//                    )
//                    .padding(20.dp)
//            ) {
//                Column {
//                    Text(
//                        text = "Admin Profile",
//                        style = MaterialTheme.typography.headlineMedium.copy(color = androidx.compose.ui.graphics.Color.White),
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Text(
//                        text = "Manage your account settings",
//                        style = MaterialTheme.typography.bodyMedium.copy(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.95f))
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // PROFILE CARD (floating)
//            Card(
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .offset(y = (-40).dp)
//            ) {
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 20.dp, horizontal = 16.dp)
//                ) {
//
//                    // Avatar + camera
//                    Box {
//                        val raw = uiState!!.avatar_url
//                        // If DB stores relative path, prefix with BASE_URL
//                        val avatarUrl = raw?.takeIf { it.isNotBlank() }?.let {
//                            if (it.startsWith("http")) it else NetworkClient.BASE_URL + it
//                        }
//
//                        if (!avatarUrl.isNullOrBlank()) {
//                            AsyncImage(
//                                model = ImageRequest.Builder(LocalContext.current)
//                                    .data(avatarUrl)
//                                    .crossfade(true)
//                                    .build(),
//                                contentDescription = "Admin avatar",
//                                modifier = Modifier
//                                    .size(96.dp)
//                                    .clip(CircleShape),
//                                contentScale = ContentScale.Crop
//                            )
//                        } else {
//                            Box(
//                                modifier = Modifier
//                                    .size(96.dp)
//                                    .clip(CircleShape)
//                                    .background(MaterialTheme.colorScheme.surfaceVariant),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                // initials
//                                val initials = uiState!!.full_name
//                                    ?.trim()
//                                    ?.split("\\s+".toRegex())
//                                    ?.mapNotNull { it.firstOrNull()?.toString() }
//                                    ?.take(2)
//                                    ?.joinToString("")
//                                    ?: ""
//                                Text(text = initials, style = MaterialTheme.typography.titleMedium)
//                            }
//                        }
//
//                        IconButton(
//                            onClick = { pickLauncher.launch("image/*") },
//                            modifier = Modifier
//                                .size(36.dp)
//                                .align(Alignment.BottomEnd)
//                                .offset(x = 8.dp, y = 8.dp)
//                                .clip(CircleShape)
//                                .background(MaterialTheme.colorScheme.primary)
//                        ) {
//                            Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Change avatar", tint = androidx.compose.ui.graphics.Color.White)
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(10.dp))
//
//                    Text(text = uiState!!.full_name ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
//                    Text(text = uiState!!.email ?: "", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    Surface(
//                        shape = RoundedCornerShape(50),
//                        color = androidx.compose.ui.graphics.Color(0xFFE6F4EA)
//                    ) {
//                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
//                            Text(text = "Admin", style = MaterialTheme.typography.labelSmall.copy(color = androidx.compose.ui.graphics.Color(0xFF1F8A3A)))
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Button(
//                        onClick = { showEditDialog = true },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 12.dp)
//                            .height(48.dp)
//                    ) {
//                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(text = "Edit Profile")
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Account Details
//            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(text = "Account Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Text(text = "Your personal and business information", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    InfoRow(icon = Icons.Default.Person, label = "Full Name", value = uiState!!.full_name ?: "")
//                    InfoRow(icon = Icons.Outlined.MailOutline, label = "Email Address", value = uiState!!.email ?: "")
//                    InfoRow(icon = Icons.Outlined.Phone, label = "Phone Number", value = uiState!!.phone ?: "")
//                    InfoRow(icon = Icons.Default.Domain, label = "Business Name", value = uiState!!.business_name ?: "")
//                    InfoRow(icon = Icons.Outlined.LocationOn, label = "Kitchen Address", value = uiState!!.address ?: "")
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Notification Preferences
//            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(text = "Notification Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
//                    Spacer(modifier = Modifier.height(6.dp))
//                    Text(text = "Manage what notifications you receive", style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    val newOrder = uiState!!.new_order_alert ?: false
//                    val lowStock = uiState!!.low_stock_alert ?: false
//                    val ai = uiState!!.ai_insights ?: false
//                    val feedback = uiState!!.customer_feedback ?: false
//
//                    ToggleRow(label = "New Order Alerts", description = "Get notified for new orders", checked = newOrder, onToggle = { checked ->
//                        viewModel.updatePreferences(UpdateAdminPrefRequest(admin_id = uiState!!.admin_id ?: 0, new_order_alerts = if (checked) 1 else 0, low_stock_alerts = if (lowStock) 1 else 0, ai_insights = if (ai) 1 else 0, customer_feedback = if (feedback) 1 else 0))
//                    })
//
//                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                    ToggleRow(label = "Low Stock Alerts", description = "Alerts when inventory is low", checked = lowStock, onToggle = { checked ->
//                        viewModel.updatePreferences(UpdateAdminPrefRequest(admin_id = uiState!!.admin_id ?: 0, new_order_alerts = if (newOrder) 1 else 0, low_stock_alerts = if (checked) 1 else 0, ai_insights = if (ai) 1 else 0, customer_feedback = if (feedback) 1 else 0))
//                    })
//
//                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                    ToggleRow(label = "AI Insights", description = "Receive AI-powered tips", checked = ai, onToggle = { checked ->
//                        viewModel.updatePreferences(UpdateAdminPrefRequest(admin_id = uiState!!.admin_id ?: 0, new_order_alerts = if (newOrder) 1 else 0, low_stock_alerts = if (lowStock) 1 else 0, ai_insights = if (checked) 1 else 0, customer_feedback = if (feedback) 1 else 0))
//                    })
//
//                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                    ToggleRow(label = "Customer Feedback", description = "New reviews and ratings", checked = feedback, onToggle = { checked ->
//                        viewModel.updatePreferences(UpdateAdminPrefRequest(admin_id = uiState!!.admin_id ?: 0, new_order_alerts = if (newOrder) 1 else 0, low_stock_alerts = if (lowStock) 1 else 0, ai_insights = if (ai) 1 else 0, customer_feedback = if (checked) 1 else 0))
//                    })
//                }
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            // Security
//            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Security")
//                        Spacer(modifier = Modifier.width(8.dp))
//                        Text(text = "Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
//                    }
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    OutlinedButton(onClick = { /* navigate to change password */ }, modifier = Modifier.fillMaxWidth()) {
//                        Text("Change Password")
//                    }
//
//                    Spacer(modifier = Modifier.height(8.dp))
//
//                    OutlinedButton(onClick = { /* navigate to 2FA */ }, modifier = Modifier.fillMaxWidth()) {
//                        Text("Two-Factor Authentication")
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(18.dp))
//
//            Button(
//                onClick = { onSignOut?.invoke() },
//                modifier = Modifier.fillMaxWidth(),
//                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFFDC2626))
//            ) {
//                Icon(imageVector = Icons.Default.ExitToApp, contentDescription = null)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Sign Out", color = androidx.compose.ui.graphics.Color.White)
//            }
//
//            Spacer(modifier = Modifier.height(40.dp))
//        }
//
//        // Edit dialog
//        if (showEditDialog) {
//            EditProfileDialog(
//                initial = uiState!!,
//                onDismiss = { showEditDialog = false },
//                onSave = { req ->
//                    viewModel.updateProfile(req)
//                    showEditDialog = false
//                }
//            )
//        }
//    }
//}
//
///** Small helpers **/
//
//@Composable
//private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
//    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
//        Surface(shape = RoundedCornerShape(8.dp), modifier = Modifier.size(44.dp), tonalElevation = 1.dp) {
//            Box(contentAlignment = Alignment.Center) { Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
//        }
//        Spacer(modifier = Modifier.width(12.dp))
//        Column {
//            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(text = value, style = MaterialTheme.typography.bodyMedium)
//        }
//    }
//}
//
//@Composable
//private fun ToggleRow(label: String, description: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
//    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
//        Column(modifier = Modifier.weight(1f)) {
//            Text(text = label, style = MaterialTheme.typography.bodyMedium)
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(text = description, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
//        }
//        Switch(checked = checked, onCheckedChange = onToggle)
//    }
//}
//
//@Composable
//private fun EditProfileDialog(initial: AdminSettingsDto, onDismiss: () -> Unit, onSave: (UpdateAdminSettingsRequest) -> Unit) {
//    var name by remember { mutableStateOf(initial.full_name ?: "") }
//    var email by remember { mutableStateOf(initial.email ?: "") }
//    var phone by remember { mutableStateOf(initial.phone ?: "") }
//    var business by remember { mutableStateOf(initial.business_name ?: "") }
//    var address by remember { mutableStateOf(initial.address ?: "") }
//
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        confirmButton = {
//            TextButton(onClick = { onSave(UpdateAdminSettingsRequest(admin_id = initial.admin_id ?: 0, full_name = name, email = email, phone = phone, business_name = business, address = address)) }) {
//                Text("Save")
//            }
//        },
//        dismissButton = {
//            TextButton(onClick = onDismiss) { Text("Cancel") }
//        },
//        title = { Text("Edit Profile") },
//        text = {
//            Column {
//                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(value = business, onValueChange = { business = it }, label = { Text("Business Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
//                Spacer(modifier = Modifier.height(8.dp))
//                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, singleLine = true, modifier = Modifier.fillMaxWidth())
//            }
//        }
//    )
//}
//
///** Utility: convert Uri -> MultipartBody.Part */
//suspend fun uriToMultipart(contentResolver: android.content.ContentResolver, uri: Uri, fieldName: String): MultipartBody.Part? {
//    return try {
//        val input: InputStream? = contentResolver.openInputStream(uri)
//        val bytes = input?.readBytes() ?: return null
//        input.close()
//        val filename = uri.lastPathSegment?.substringAfterLast('/') ?: "avatar.jpg"
//        val requestBody: RequestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
//        MultipartBody.Part.createFormData(fieldName, filename, requestBody)
//    } catch (t: Throwable) {
//        t.printStackTrace()
//        null
//    }
//}
