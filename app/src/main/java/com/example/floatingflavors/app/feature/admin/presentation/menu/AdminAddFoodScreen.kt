// File: app/src/main/java/com/example/floatingflavors/app/feature/admin/presentation/menu/AdminAddFoodScreen.kt
package com.example.floatingflavors.app.feature.admin.presentation.menu

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.floatingflavors.app.feature.menu.presentation.MenuViewModel
import java.io.File

@Composable
fun AdminAddFoodScreen(
    onBackClick: () -> Unit = {},
    onAdded: () -> Unit = {}
) {
    val vm: MenuViewModel = viewModel()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var pickedFile by remember { mutableStateOf<File?>(null) }

    val isLoading = vm.isLoading
    val error = vm.errorMessage

    // Image picker launcher (ACTION_GET_CONTENT)
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        pickedUri = uri
        // convert to File in cache
        pickedFile = uri?.let { uriToFile(context, it, "upload_${System.currentTimeMillis()}.jpg") }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Add Food Item", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))

        // Image preview + pick button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (pickedUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = pickedUri),
                    contentDescription = "picked image",
                    modifier = Modifier.size(96.dp)
                )
            } else {
                Box(modifier = Modifier.size(96.dp))
            }

            Column(modifier = Modifier.fillMaxWidth().padding(start = 8.dp)) {
                Button(onClick = { launcher.launch("image/*") }) {
                    Text("Pick Image")
                }
                Text(text = "Max 5MB. JPG/PNG/WEBP")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            val price = priceText.toDoubleOrNull()
            if (name.isBlank() || price == null) {
                // you can show a snackbar or set vm.errorMessage via a function
                return@Button
            }

            // If file selected -> use multipart upload, else use addMenuItem with onSuccess lambda
            if (pickedFile != null) {
                vm.addMenuItemWithImage(name, description, price, category, pickedFile) {
                    // success
                    name = ""; description = ""; priceText = ""; category = ""; pickedUri = null; pickedFile = null
                    onAdded()
                }
            } else {
                // NOTE: pass the success lambda as the last parameter (previously you passed a string)
                vm.addMenuItem(name, description, price, category) {
                    // success (no image)
                    name = ""; description = ""; priceText = ""; category = ""
                    onAdded()
                }
            }
        }, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Add Food")
        }

        if (!error.isNullOrEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

// Helper: convert Uri to File by copying into cache dir
fun uriToFile(context: Context, uri: Uri, filename: String): File {
    val input = context.contentResolver.openInputStream(uri)!!
    val file = File(context.cacheDir, filename)
    file.outputStream().use { output ->
        input.copyTo(output)
    }
    return file
}
