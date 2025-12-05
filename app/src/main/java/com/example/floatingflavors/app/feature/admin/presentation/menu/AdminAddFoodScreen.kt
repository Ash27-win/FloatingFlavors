package com.example.floatingflavors.app.feature.admin.presentation.menu

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.lang.Exception

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddFoodScreen(
    navController: NavController,
    vm: MenuViewModel = viewModel()
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var initialStock by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }

    val isLoading = vm.isLoading
    val error = vm.errorMessage

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        if (uri != null) {
            scope.launch {
                imageFile = uriToFile(ctx, uri)
            }
        } else {
            imageFile = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add New Food Item") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Subheading
            Text(
                text = "Fill in the details to add a new item to your menu",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name") },
                placeholder = { Text("e.g., Paneer Tikka") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                placeholder = { Text("e.g., Starters") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = priceText,
                onValueChange = { input -> priceText = input.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Price (₹)") },
                placeholder = { Text("280") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Describe your dish...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = initialStock,
                onValueChange = { initialStock = it.filter { c -> c.isDigit() } },
                label = { Text("Initial Stock") },
                placeholder = { Text("20") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (imageUri == null) "Click to upload image" else "Change Image")
            }

            Spacer(Modifier.height(12.dp))

            if (imageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx).data(imageUri).crossfade(true).build(),
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(12.dp))
            }

            if (!error.isNullOrEmpty()) {
                Text(error ?: "", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    val priceDouble = priceText.toDoubleOrNull()
                    if (name.isBlank() || priceDouble == null) {
                        // Show small inline validation
                        vm.setError("Please provide a name and valid numeric price")
                        return@Button
                    }

                    // call ViewModel to upload (imageFile may be null)
                    vm.addMenuItemWithImage(
                        name = name.trim(),
                        description = description.trim(),
                        price = priceDouble,
                        category = category.trim().ifEmpty { "General" },
                        imageFile = imageFile,
                        onSuccess = {
                            // notify previous screen to refresh list
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("menu_refresh", true)

                            navController.popBackStack()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Uploading...")
                } else {
                    Text("Add Item")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Convert Uri → File (runs on IO dispatcher) */
suspend fun uriToFile(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
    return@withContext try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream == null) return@withContext null
        val cacheFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        FileOutputStream(cacheFile).use { out -> inputStream.copyTo(out) }
        cacheFile
    } catch (e: Exception) {
        Log.e("AdminAddFood", "uriToFile error", e)
        null
    }
}
