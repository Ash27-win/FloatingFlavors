package com.example.floatingflavors.app.feature.delivery.presentation.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryDocumentDto
import com.example.floatingflavors.app.feature.delivery.data.remote.SimpleResponseDto
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

sealed class DeliveryDocumentsUiState {
    object Loading : DeliveryDocumentsUiState()
    data class Success(val documents: List<DeliveryDocumentDto>) : DeliveryDocumentsUiState()
    data class Error(val message: String) : DeliveryDocumentsUiState()
}

class DeliveryDocumentsViewModel(
    private val application: Application,
    private val deliveryPartnerId: Int,
    private val repository: DeliveryRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading

    private val _uiState = MutableStateFlow<DeliveryDocumentsUiState>(DeliveryDocumentsUiState.Loading)
    val uiState: StateFlow<DeliveryDocumentsUiState> = _uiState

    init {
        loadDocuments()
    }

    fun loadDocuments() {
        viewModelScope.launch {
            try {
                _uiState.value = DeliveryDocumentsUiState.Loading
                val response = repository.getDocuments()
                
                if (response.success && response.data != null) {
                    response.data.forEach { doc ->
                        android.util.Log.d("DeliveryDocs", "Fetched Active Document: ${doc.type} -> URL: ${doc.documentUrl}")
                    }
                    _uiState.value = DeliveryDocumentsUiState.Success(response.data)
                } else {
                    // Dummy data fallback if success is false but didn't throw
                    _uiState.value = DeliveryDocumentsUiState.Success(
                        listOf(
                            DeliveryDocumentDto("license", null, "verified"),
                            DeliveryDocumentDto("aadhaar", null, "pending"),
                            DeliveryDocumentDto("vehicle_rc", null, "verified"),
                            DeliveryDocumentDto("vehicle_insurance", null, "rejected")
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = DeliveryDocumentsUiState.Error("Failed to connect to server: ${e.localizedMessage}")
            }
        }
    }

    fun uploadDocument(uri: Uri, type: String) {
        viewModelScope.launch {
            try {
                _isUploading.value = true
                val mimeType = application.contentResolver.getType(uri) ?: "application/octet-stream"
                val file = getFileFromUri(uri, mimeType)
                
                if (file == null) {
                    _events.emit("Failed to process file")
                    _isUploading.value = false
                    return@launch
                }

                // Strict Document Validation
                val isPdf = mimeType.contains("pdf", ignoreCase = true)
                val validationResult = com.example.floatingflavors.app.feature.delivery.presentation.utils.DocumentValidator.validateDocument(file, isPdf)
                
                if (validationResult is com.example.floatingflavors.app.feature.delivery.presentation.utils.ValidationResult.Invalid) {
                    _events.emit(validationResult.reason)
                    if (file.exists()) file.delete() // Clean up local cache
                    _isUploading.value = false
                    return@launch
                }

                android.util.Log.d("DeliveryDocs", "-- UPLOADING NEW DOCUMENT --")
                android.util.Log.d("DeliveryDocs", "Type: $type")
                android.util.Log.d("DeliveryDocs", "File Name: ${file.name}")
                android.util.Log.d("DeliveryDocs", "File Size: ${file.length() / 1024} KB")

                val result = repository.uploadDocument(type, file, mimeType)
                if (result.success) {
                    _events.emit("Document updated successfully")
                    loadDocuments()
                } else {
                    _events.emit(result.message ?: "Upload failed. Try again.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit("Upload failed. Try again.")
            } finally {
                _isUploading.value = false
            }
        }
    }

    private fun getFileFromUri(uri: Uri, mimeType: String): File? {
        val extension = when {
            mimeType.contains("pdf") -> "pdf"
            mimeType.contains("png") -> "png"
            mimeType.contains("webp") -> "webp"
            mimeType.contains("word") || mimeType.contains("officedocument") -> "docx"
            else -> "jpg"
        }
        
        return try {
            val inputStream: InputStream? = application.contentResolver.openInputStream(uri)
            val file = File(application.cacheDir, "temp_doc_${System.currentTimeMillis()}.$extension")
            val outputStream = FileOutputStream(file)
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            null
        }
    }
}
