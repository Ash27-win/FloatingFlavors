package com.example.floatingflavors.app.feature.user.presentation.settings

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import okhttp3.MultipartBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    LaunchedEffect(viewModel.uiState) {
        when (val s = viewModel.uiState) {
            is EditProfileUiState.Success -> {
                Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
                onBack()
            }
            is EditProfileUiState.Error -> {
                Toast.makeText(context, s.message, Toast.LENGTH_SHORT).show()
            }
            else -> {}
        }
    }

    val imagePicker =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            viewModel.pickedImageUri = it
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FAFB))
    ) {

        /* ---------------- HEADER (ORDER SCREEN STYLE) ---------------- */

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(Color.White)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Edit Profile",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }
        }

        /* ---------------- CONTENT ---------------- */

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {

            /* PROFILE IMAGE */

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box {
                    AsyncImage(
                        model = viewModel.pickedImageUri ?: viewModel.profileImageUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE5E7EB)),
                        contentScale = ContentScale.Crop
                    )

                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Image",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(6.dp, 6.dp)
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { imagePicker.launch("image/*") }
                            .padding(6.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Field("Full Name*", viewModel.name) { viewModel.name = it }
            Field("Phone*", viewModel.phone) { viewModel.phone = it }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Field("Pincode*", viewModel.pincode) { viewModel.pincode = it }
                }
                Column(Modifier.weight(1f)) {
                    Field("City*", viewModel.city) { viewModel.city = it }
                }
            }

            Field("House*", viewModel.house) { viewModel.house = it }
            Field("Area*", viewModel.area) { viewModel.area = it }
            Field("Landmark", viewModel.landmark) { viewModel.landmark = it }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = {
                    val imagePart = viewModel.pickedImageUri?.let {
                        val file = uriToFile(context, it)
                        MultipartBody.Part.createFormData(
                            "profile_image",
                            file.name,
                            file.asRequestBody("image/*".toMediaType())
                        )
                    }
                    viewModel.submitProfile(imagePart)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1CC339))
            ) {
                Text("Save Changes", fontSize = 16.sp)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* ---------------- HELPERS ---------------- */

@Composable
private fun Field(label: String, value: String, onChange: (String) -> Unit) {
    Text(label, fontSize = 12.sp, color = Color.Gray)
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp)
    )
    Spacer(Modifier.height(12.dp))
}

private fun uriToFile(context: Context, uri: Uri): File {
    val input = context.contentResolver.openInputStream(uri)!!
    val file = File(context.cacheDir, "img_${System.currentTimeMillis()}")
    file.outputStream().use { input.copyTo(it) }
    return file
}
