package com.example.floatingflavors.app.feature.delivery.presentation

class DeliveryOrderDetailsViewModelFactory(
    private val orderId: Int,
    private val deliveryPartnerId: Int
) : androidx.lifecycle.ViewModelProvider.Factory {

    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryOrderDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryOrderDetailsViewModel(orderId, deliveryPartnerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
