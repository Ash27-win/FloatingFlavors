package com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.settings.AddressRepository
import com.example.floatingflavors.app.feature.user.data.settings.dto.EditAddressRequest
import kotlinx.coroutines.launch

class EditAddressViewModel(
    private val repo: AddressRepository
) : ViewModel() {

    var snackbarMessage by mutableStateOf<String?>(null)
        private set

    fun updateAddress(
        req: EditAddressRequest,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val res = repo.updateAddress(req)
                if (res.status) {
                    snackbarMessage = "Address updated successfully"
                    onSuccess()
                } else {
                    snackbarMessage = res.message
                }
            } catch (e: Exception) {
                snackbarMessage = e.message ?: "Something went wrong"
            }
        }
    }

    fun clearSnackbar() {
        snackbarMessage = null
    }
}

