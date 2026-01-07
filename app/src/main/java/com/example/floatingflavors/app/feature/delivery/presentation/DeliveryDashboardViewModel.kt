package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.delivery.data.DeliveryDashboardState
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.OrderDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeliveryDashboardViewModel(
    private val repository: DeliveryRepository,
    private val deliveryPartnerId: Int
) : ViewModel() {

    private val _state = MutableStateFlow<DeliveryDashboardState?>(null)
    val state: StateFlow<DeliveryDashboardState?> = _state

    fun loadDashboard() {
        viewModelScope.launch {
            val response = repository.getDashboard(deliveryPartnerId)

            if (response.success) {
                _state.value = DeliveryDashboardState(
                    deliveryPartnerName = response.deliveryPartnerName ?: "Delivery Partner",

                    // ✅ ACTIVE ORDER (NO PICKUP / DROP)
                    activeOrder = response.activeOrder?.let {
                        OrderDto(
                            id = it.id?.toIntOrNull() ?: 0,
                            customerName = it.customerName ?: "",
                            customerPhone = it.customerPhone, // ✅ NOW EXISTS
                            pickupAddress = "",
                            dropAddress = "",
                            amount = it.amount?.toIntOrNull() ?: 0,
                            status = it.status
                        )
                    },

                    // ✅ UPCOMING ORDERS
                    upcomingOrders = response.upcomingOrders?.map {
                        OrderDto(
                            id = it.id?.toIntOrNull() ?: 0,
                            customerName = it.customerName ?: "",
                            customerPhone = null,
                            pickupAddress = it.pickupAddress ?: "",
                            dropAddress = it.dropAddress ?: "",
                            amount = it.amount?.toIntOrNull() ?: 0,
                            status = it.status
                        )
                    } ?: emptyList()
                )
            }
        }
    }
}
