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
            try {
                // Also refresh notifications when dashboard loads
                launch { 
                    try {
                        notificationRepository.refreshNotifications() 
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                // Fetch Dashboard and Earnings in parallel
                val dashboardJob = launch {
                    try {
                        val response = repository.getDashboard(deliveryPartnerId)
                        if (response.success) {
                            // Process mapping on Default dispatcher to keep UI responsive
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Default) {
                                val stateValue = DeliveryDashboardState(
                                    deliveryPartnerName = response.deliveryPartnerName ?: "Delivery Partner",
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
                                _state.value = stateValue
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                val earningsJob = launch {
                    try {
                        val earningRes = NetworkClient.deliveryApi.getDriverEarnings(deliveryPartnerId)
                        if (earningRes.success) {
                            _earnings.value = earningRes
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
