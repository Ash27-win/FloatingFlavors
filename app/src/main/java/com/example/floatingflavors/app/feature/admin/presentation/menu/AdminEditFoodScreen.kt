package com.example.floatingflavors.app.feature.admin.presentation.menu

import android.net.Uri
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.floatingflavors.app.core.ui.util.uriToFile
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEditFoodScreen(
    navController: NavController,
    itemId: Int,
    vm: MenuViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // VM state
    val items by remember { derivedStateOf { vm.menuItems } }
    val isLoading by remember { derivedStateOf { vm.isLoading } }
    val errorMessage by remember { derivedStateOf { vm.errorMessage } }

    // Ensure menu is loaded if item missing
    var triedLoadOnce by remember { mutableStateOf(false) }
    val menuItem: MenuItemDto? = items.find { it.id?.toIntOrNull() == itemId }

    LaunchedEffect(itemId) {
        if (menuItem == null && !triedLoadOnce) {
            triedLoadOnce = true
            vm.loadMenu()
        }
    }

    // picked image and file
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var pickedFile by remember { mutableStateOf<File?>(null) }

    // Temporary form fields (lifted to top-level so bottom save button can access them)
    var tmpName by remember { mutableStateOf("") }
    var tmpCategory by remember { mutableStateOf("") }
    var tmpDescription by remember { mutableStateOf("") }
    var tmpPrice by remember { mutableStateOf("") }
    var tmpAvailable by remember { mutableStateOf(true) }
    var currentImageUrl by remember { mutableStateOf<String?>(null) }

    // When menuItem becomes available, initialize tmp fields once
    LaunchedEffect(menuItem) {
        menuItem?.let { it ->
            // Initialize only if blank (so navigating back doesn't overwrite user's edits)
            if (tmpName.isBlank()) tmpName = it.name ?: ""
            if (tmpCategory.isBlank()) tmpCategory = it.category ?: ""
            if (tmpDescription.isBlank()) tmpDescription = it.description ?: ""
            if (tmpPrice.isBlank()) tmpPrice = it.price ?: ""
            tmpAvailable = (it.is_available?.toIntOrNull() ?: 0) == 1
            if (currentImageUrl.isNullOrBlank()) currentImageUrl = it.image_full ?: it.image_url
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    // image picker
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            pickedUri = uri
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val file = uriToFile(context, uri, "upload_${System.currentTimeMillis()}.jpg")
                    coroutineScope.launch { pickedFile = file }
                } catch (e: Exception) {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Image read error: ${e.message}") }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Food Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },

        // Move Save button into bottomBar so content can scroll beneath it safely
        bottomBar = {
            // give the bottomBar some padding so it looks like your previous design
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing) // respect system bars
            ) {
                Button(
                    onClick = {
                        val priceDouble = tmpPrice.toDoubleOrNull()
                        coroutineScope.launch {
                            vm.editMenuItem(
                                id = itemId,
                                name = tmpName.takeIf { it.isNotBlank() },
                                description = tmpDescription.takeIf { it.isNotBlank() },
                                price = priceDouble,
                                category = tmpCategory.takeIf { it.isNotBlank() },
                                isAvailable = if (tmpAvailable) 1 else 0,
                                imageFile = pickedFile
                            )
                            navController.previousBackStackEntry?.savedStateHandle?.set("menu_refresh", true)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00A651)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Save", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save Changes", color = Color.White)
                }
            }
        }
    ) { innerPadding ->
        // Loading / missing item handling
        if ((menuItem == null) && (isLoading || !triedLoadOnce)) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (menuItem == null) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), contentAlignment = Alignment.Center) {
                Text("Item not found or failed to load.", color = Color.Gray)
            }
            return@Scaffold
        }

        // Content: make it scrollable and use innerPadding (includes bottomBar height)
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding)           // ensures space for bottomBar/topBar
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .imePadding(),                    // push content above IME (keyboard)
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // IMAGE
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                val imageModifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF111111))

                if (pickedUri != null) {
                    AsyncImage(model = pickedUri, contentDescription = "Picked Image", modifier = imageModifier, contentScale = ContentScale.Crop)
                } else if (!currentImageUrl.isNullOrBlank()) {
                    AsyncImage(model = currentImageUrl, contentDescription = menuItem.name, modifier = imageModifier, contentScale = ContentScale.Crop)
                } else {
                    Box(imageModifier, contentAlignment = Alignment.Center) { Text("No Image", color = Color.LightGray) }
                }

                IconButton(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-8).dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF00A651))
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Change Image", tint = Color.White)
                }
            }

            // FORM FIELDS bound to tmp* states
            OutlinedTextField(value = tmpName, onValueChange = { tmpName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tmpCategory, onValueChange = { tmpCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tmpDescription, onValueChange = { tmpDescription = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth().height(140.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tmpPrice,
                    onValueChange = { s -> tmpPrice = s.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Price") },
                    modifier = Modifier.weight(1f)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Stock", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("-", modifier = Modifier.padding(8.dp))
                        Text("25", modifier = Modifier.padding(horizontal = 6.dp))
                        Text("+", modifier = Modifier.padding(8.dp))
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Available", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Switch(checked = tmpAvailable, onCheckedChange = {
                    tmpAvailable = it
                    coroutineScope.launch { vm.toggleAvailability(itemId, if (it) 1 else 0) }
                })
            }

            // placeholder rating row (ensure reachable by scrolling)
//            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
//                Text("Rating", modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
//                Text(menuItem.rating ?: "—", fontSize = 16.sp)
//            }

            Spacer(modifier = Modifier.height(8.dp))
            // end of column content - innerPadding ensures bottom space for bottomBar
        }
    }
}

// suspend confirm dialog using Android AlertDialog (works inside Compose coroutine)
private suspend fun showDeleteConfirm(context: Context): Boolean {
    return suspendCancellableCoroutine { cont ->
        val builder = android.app.AlertDialog.Builder(context)
        builder.setTitle("Delete item")
        builder.setMessage("Are you sure you want to delete this item? This cannot be undone.")
        builder.setPositiveButton("Delete") { _, _ -> if (cont.isActive) cont.resume(true) }
        builder.setNegativeButton("Cancel") { _, _ -> if (cont.isActive) cont.resume(false) }
        builder.setOnCancelListener { if (cont.isActive) cont.resume(false) }
        val d = builder.create()
        d.show()
        cont.invokeOnCancellation { d.dismiss() }
    }
}
