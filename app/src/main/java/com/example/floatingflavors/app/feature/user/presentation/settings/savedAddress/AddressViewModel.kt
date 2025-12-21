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

    fun load(userId: Int) = viewModelScope.launch {
        addresses = repo.load(userId).data ?: emptyList()
    }

    fun add(
        userId: Int,
        label: String,
        house: String,
        area: String,
        pincode: String,
        city: String,
        landmark: String?,
        onDone: () -> Unit
    ) = viewModelScope.launch {
        repo.add(userId, label, house, area, pincode, city, landmark)
        onDone()
    }

    fun delete(addressId: Int, userId: Int) = viewModelScope.launch {
        repo.delete(addressId)
        load(userId)
    }
}
