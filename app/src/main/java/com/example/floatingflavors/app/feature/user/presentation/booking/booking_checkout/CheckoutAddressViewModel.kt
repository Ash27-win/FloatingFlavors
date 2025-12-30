package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.booking_checkout.AddressCheckoutRepository
import com.example.floatingflavors.app.feature.user.data.booking_checkout.dto.AddressCheckoutDto
import kotlinx.coroutines.launch

class CheckoutAddressViewModel(
    private val repo: AddressCheckoutRepository
) : ViewModel() {

    var addresses by mutableStateOf<List<AddressCheckoutDto>>(emptyList())
        private set

    var selectedAddressId by mutableStateOf<Int?>(null)
        private set

    fun load(userId: Int) {
        viewModelScope.launch {
            try {
                addresses = repo.getAddresses(userId)
            } catch (e: Exception) {
                // 🔥 PREVENT APP CRASH
                addresses = emptyList()

                android.util.Log.e(
                    "CHECKOUT_ADDRESS_ERROR",
                    "Failed to load addresses for userId=$userId",
                    e
                )
            }
        }
    }


    fun select(userId: Int, addressId: Int) {
        viewModelScope.launch {
            if (repo.setDefault(userId, addressId)) {
                selectedAddressId = addressId
                addresses = addresses.map {
                    it.copy(is_default = if (it.id == addressId) 1 else 0)
                }
            }
        }
    }
}
