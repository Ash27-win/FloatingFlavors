package com.example.floatingflavors.app.feature.delivery.presentation.profile

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.remote.dto.DeliveryProfileDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DeliveryProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DeliveryRepository(NetworkClient.deliveryApi)

    private val _uiState = MutableStateFlow<DeliveryProfileUiState>(DeliveryProfileUiState.Loading)
    val uiState: StateFlow<DeliveryProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    // Form State (for editing)
    var editName = MutableStateFlow("")
    var editEmail = MutableStateFlow("")
    var editPhone = MutableStateFlow("") // Read-only usually
    var editEmergency = MutableStateFlow("")
    var editImageUri = MutableStateFlow<Uri?>(null) // Local picked image

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = DeliveryProfileUiState.Loading
                val response = repository.getProfile()
                if (response.success && response.profile != null) {
                    _uiState.value = DeliveryProfileUiState.Success(response.profile)
                    // Pre-fill form state
                    updateFormState(response.profile)
                } else {
                    _uiState.value = DeliveryProfileUiState.Error(response.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = DeliveryProfileUiState.Error("Connection Failed: ${e.localizedMessage}")
            }
        }
    }

    private fun updateFormState(profile: DeliveryProfileDto) {
        editName.value = profile.name
        editEmail.value = profile.email
        editPhone.value = profile.phone
        editEmergency.value = profile.emergencyContact
    }

    fun updateProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = DeliveryProfileUiState.Loading // Or keep success but show progress check

                // Convert Uri to File if new image picked
                var imageFile: File? = null
                editImageUri.value?.let { uri ->
                    imageFile = getFileFromUri(uri)
                }

                val response = repository.updateProfile(
                    name = editName.value,
                    email = editEmail.value,
                    phone = editPhone.value,
                    emergencyContact = editEmergency.value,
                    imageFile = imageFile
                )

                if (response.success) {
                    _events.emit("Profile Updated Successfully")
                    loadProfile() // Refresh to get new image URL if needed
                    onSuccess()
                } else {
                    _events.emit(response.message)
                    // Reload state to ensure consistent UI
                    loadProfile() 
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit("Update Failed: Connection Error")
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val context = getApplication<Application>()
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_profile_upload.jpg")
            val outputStream = FileOutputStream(file)
            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

sealed class DeliveryProfileUiState {
    object Loading : DeliveryProfileUiState()
    data class Success(val profile: DeliveryProfileDto) : DeliveryProfileUiState()
    data class Error(val message: String) : DeliveryProfileUiState()
}
