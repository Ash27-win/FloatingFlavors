package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.delivery.data.DeliveryDashboardState
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.OrderDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DeliveryDashboardViewModel(
    private val repository: DeliveryRepository,
    private val notificationRepository: com.example.floatingflavors.app.feature.notification.data.NotificationRepository,
    private val deliveryPartnerId: Int
) : ViewModel() {

    private val _state = MutableStateFlow<DeliveryDashboardState?>(null)
    val state: StateFlow<DeliveryDashboardState?> = _state

    private val _navigationEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent
    
    // ✅ Unread Count for Badge
    val unreadCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    private val _earnings = MutableStateFlow<com.example.floatingflavors.app.feature.delivery.data.remote.DriverEarningsResponse?>(null)
    val earnings: StateFlow<com.example.floatingflavors.app.feature.delivery.data.remote.DriverEarningsResponse?> = _earnings

    fun loadDashboard() {
        viewModelScope.launch {
            // Also refresh notifications when dashboard loads
            launch { notificationRepository.refreshNotifications() }
            
            // Fetch Dashboard
            val response = repository.getDashboard(deliveryPartnerId)

            if (response.success) {
                _state.value = DeliveryDashboardState(
                    deliveryPartnerName = response.deliveryPartnerName ?: "Delivery Partner",

                    // ✅ ACTIVE ORDER (NO PICKUP / DROP)
                    activeOrder = response.activeOrder?.let {
                        OrderDto(
                            id = it.id?.toIntOrNull() ?: 0,
                            customerName = it.customerName ?: "",
                            customerPhone = it.customerPhone,
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
            
            // ✅ Fetch Earnings
            try {
                val earningRes = NetworkClient.deliveryApi.getDriverEarnings(deliveryPartnerId)
                if (earningRes.success) {
                    _earnings.value = earningRes
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun acceptOrder(orderId: Int) {
        viewModelScope.launch {
            try {
               val res = repository.acceptOrder(orderId, deliveryPartnerId)
               if (res.success) {
                   // Navigate to Tracking
                   _navigationEvent.emit(orderId.toString())
                   loadDashboard() // Refresh
               }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun refreshOrders() {
        loadDashboard()
    }
}
