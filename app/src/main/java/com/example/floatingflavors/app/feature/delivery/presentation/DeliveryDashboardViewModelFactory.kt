package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository

class DeliveryDashboardViewModelFactory(
    private val repository: DeliveryRepository,
    private val notificationRepository: com.example.floatingflavors.app.feature.notification.data.NotificationRepository,
    private val deliveryPartnerId: Int
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryDashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryDashboardViewModel(
                repository = repository,
                notificationRepository = notificationRepository,
                deliveryPartnerId = deliveryPartnerId
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
