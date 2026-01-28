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
        context: android.content.Context,
        req: EditAddressRequest,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            var lat = req.latitude
            var long = req.longitude
            
            // Try to geocode only if address fields changed or lat/long is 0
            // For simplicity, we can just re-geocode always on update to ensure accuracy
            try {
                val addressString = "${req.house}, ${req.area}, ${req.city}, ${req.pincode}"
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                     val geocoder = android.location.Geocoder(context)
                     val addresses = geocoder.getFromLocationName(addressString, 1)
                     if (!addresses.isNullOrEmpty()) {
                         lat = addresses[0].latitude
                         long = addresses[0].longitude
                     }
                } else {
                    @Suppress("DEPRECATION")
                    val geocoder = android.location.Geocoder(context)
                    val addresses = geocoder.getFromLocationName(addressString, 1)
                    if (!addresses.isNullOrEmpty()) {
                        lat = addresses[0].latitude
                        long = addresses[0].longitude
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val finalReq = req.copy(latitude = lat, longitude = long)

            try {
                val res = repo.updateAddress(finalReq)
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (res.status) {
                        snackbarMessage = "Address updated successfully"
                        onSuccess()
                    } else {
                        snackbarMessage = res.message
                    }
                }
            } catch (e: Exception) {
                 kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    snackbarMessage = e.message ?: "Something went wrong"
                 }
            }
        }
    }

    fun clearSnackbar() {
        snackbarMessage = null
    }
}

