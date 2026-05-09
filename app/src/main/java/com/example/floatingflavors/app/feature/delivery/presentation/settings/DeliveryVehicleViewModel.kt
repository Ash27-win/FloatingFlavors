package com.example.floatingflavors.app.feature.delivery.presentation.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryVehicleDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

sealed class DeliveryVehicleUiState {
    object Loading : DeliveryVehicleUiState()
    data class Success(val vehicleInfo: DeliveryVehicleDto) : DeliveryVehicleUiState()
    data class Error(val message: String) : DeliveryVehicleUiState()
}

class DeliveryVehicleViewModel(
    private val application: Application,
    private val deliveryPartnerId: Int,
    private val repository: DeliveryRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<DeliveryVehicleUiState>(DeliveryVehicleUiState.Loading)
    val uiState: StateFlow<DeliveryVehicleUiState> = _uiState

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    // Form State
    var editVehicleType = MutableStateFlow("")
    var editModelName = MutableStateFlow("")
    var editVehicleNumber = MutableStateFlow("")
    var editRegistrationYear = MutableStateFlow("")
    var editInsuranceExpiry = MutableStateFlow("")

    init {
        loadVehicleInfo()
    }

    private fun updateFormState(vehicle: DeliveryVehicleDto) {
        editVehicleType.value = vehicle.vehicleType ?: ""
        editModelName.value = vehicle.modelName ?: ""
        editVehicleNumber.value = vehicle.vehicleNumber ?: ""
        editRegistrationYear.value = vehicle.registrationYear ?: ""
        
        // Convert MySQL "yyyy-MM-dd" to "dd MMM yyyy" for UI & PHP saving
        editInsuranceExpiry.value = try {
            val dateStr = vehicle.insuranceExpiryDate
            if (!dateStr.isNullOrEmpty()) {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                val date = inputFormat.parse(dateStr)
                date?.let { outputFormat.format(it) } ?: dateStr
            } else ""
        } catch (e: Exception) {
            vehicle.insuranceExpiryDate ?: ""
        }
    }

    fun loadVehicleInfo() {
        viewModelScope.launch {
            try {
                _uiState.value = DeliveryVehicleUiState.Loading
                val response = repository.getVehicleInfo()
                
                if (response.success && response.data != null) {
                    _uiState.value = DeliveryVehicleUiState.Success(response.data)
                    updateFormState(response.data)
                } else {
                    // Mock data fallback if needed
                    val mockVehicle = DeliveryVehicleDto(
                        vehicleType = "Two Wheeler (Scooter)",
                        modelName = "Honda Activa 6G",
                        vehicleNumber = "KA 01 EB 1234",
                        registrationYear = "2023",
                        insuranceExpiryDate = "Expires: 12 Oct 2025",
                        vehicleImage = null
                    )
                    _uiState.value = DeliveryVehicleUiState.Success(mockVehicle)
                    updateFormState(mockVehicle)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = DeliveryVehicleUiState.Error("Failed to connect: ${e.localizedMessage}")
            }
        }
    }

    fun saveVehicleInfo() {
        viewModelScope.launch {
            try {
                val request = com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryVehicleUpdateRequest(
                    vehicleType = editVehicleType.value,
                    modelName = editModelName.value,
                    vehicleNumber = editVehicleNumber.value,
                    registrationYear = editRegistrationYear.value,
                    insuranceExpiryDate = editInsuranceExpiry.value
                )
                val response = repository.updateVehicleInfo(request)
                if (response.success) {
                    _events.emit("Vehicle details updated successfully")
                    loadVehicleInfo()
                } else {
                    _events.emit("Update failed: ${response.message}")
                }
            } catch (e: Exception) {
                _events.emit("Error: ${e.message}")
            }
        }
    }

    fun uploadVehicleImage(uri: Uri) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val file = getFileFromUri(uri)
                if (file != null) {
                    // Strict Validation
                    val validationResult = com.example.floatingflavors.app.feature.delivery.presentation.utils.DocumentValidator.validateImage(file)
                    if (validationResult is com.example.floatingflavors.app.feature.delivery.presentation.utils.ValidationResult.Invalid) {
                        _events.emit(validationResult.reason)
                        if (file.exists()) file.delete()
                        _isUploading.value = false
                        return@launch
                    }

                    val currentState = _uiState.value
                    val vehicleType = if (currentState is DeliveryVehicleUiState.Success) {
                        currentState.vehicleInfo.vehicleType
                    } else "GENERAL"

                    val response = repository.updateVehicleImage(vehicleType, file)
                    if (response.success) {
                        _events.emit("Vehicle image updated successfully!")
                        loadVehicleInfo()
                    } else {
                        _events.emit("Upload failed: ${response.message}")
                    }
                } else {
                    _events.emit("Could not process file")
                }
            } catch (e: Exception) {
                _events.emit("Error: ${e.message}")
            } finally {
                _isUploading.value = false
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val contentResolver = application.contentResolver
        val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
        val extension = when {
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("png") -> "png"
            mimeType.contains("webp") -> "webp"
            else -> "jpg"
        }
        
        val fileName = "vehicle_${System.currentTimeMillis()}.$extension"
        val file = File(application.cacheDir, fileName)
        
        return try {
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}
