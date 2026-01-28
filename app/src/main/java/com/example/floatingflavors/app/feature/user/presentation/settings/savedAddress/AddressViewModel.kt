package com.example.floatingflavors.app.feature.user.presentation.settings.savedAddress

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.settings.AddressRepository
import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto
import kotlinx.coroutines.launch

class AddressViewModel(
    private val repo: AddressRepository
) : ViewModel() {

    var addresses: List<AddressDto> by mutableStateOf(emptyList())
        private set

    var refreshTrigger by mutableStateOf(0)
        private set

    fun load(userId: Int) = viewModelScope.launch {
        addresses = repo.load(userId).data ?: emptyList()
    }

    fun add(
        context: android.content.Context,
        userId: Int,
        label: String,
        house: String,
        area: String,
        pincode: String,
        city: String,
        landmark: String?,
        onDone: () -> Unit
    ) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        var lat = 0.0
        var long = 0.0
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                // For Android 13+ (Tiramisu)
                val geocoder = android.location.Geocoder(context)
                 val addresses = geocoder.getFromLocationName("$house, $area, $city, $pincode", 1)
                 if (!addresses.isNullOrEmpty()) {
                     lat = addresses[0].latitude
                     long = addresses[0].longitude
                 }
            } else {
                // For older versions
                @Suppress("DEPRECATION")
                val geocoder = android.location.Geocoder(context)
                val addresses = geocoder.getFromLocationName("$house, $area, $city, $pincode", 1)
                if (!addresses.isNullOrEmpty()) {
                    lat = addresses[0].latitude
                    long = addresses[0].longitude
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        repo.add(userId, label, house, area, pincode, city, landmark, lat, long)
        // Switch back to Main for UI updates if needed, though refreshTrigger is state
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            refreshTrigger++
            onDone()
        }
    }

    fun delete(addressId: Int, userId: Int) = viewModelScope.launch {
        repo.delete(addressId)
        load(userId)
    }

    fun setDefault(addressId: Int, userId: Int) = viewModelScope.launch {
        repo.setDefault(addressId, userId)
        load(userId) // 🔥 refresh list
    }
}

