package com.example.floatingflavors.app.feature.delivery.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository

class DeliverySettingsViewModelFactory(
    private val application: android.app.Application,
    private val deliveryPartnerId: Int,
    private val repository: DeliveryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryDocumentsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryDocumentsViewModel(application, deliveryPartnerId, repository) as T
        }
        if (modelClass.isAssignableFrom(DeliveryVehicleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryVehicleViewModel(application, deliveryPartnerId, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
