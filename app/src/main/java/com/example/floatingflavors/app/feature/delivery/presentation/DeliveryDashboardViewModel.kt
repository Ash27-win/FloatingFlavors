package com.example.floatingflavors.app.feature.delivery.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DeliveryDashboardViewModel(
    private val repository: DeliveryRepository,
    private val deliveryPartnerId: Int
) : ViewModel() {

    private var isDashboardLoaded = false

    private val _state = MutableStateFlow<DeliveryDashboardState?>(null)
    val state: StateFlow<DeliveryDashboardState?> = _state

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadDashboard() {

        if (isDashboardLoaded) return   // 👈 PREVENT DOUBLE CALL

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = repository.getDashboard(deliveryPartnerId)
                if (response.success) {
                    // Convert API response to UI state
                    _state.value = DeliveryDashboardState(
                        deliveryPartnerName = response.deliveryPartnerName ?: "Delivery Partner",
                        activeOrder = response.activeOrder?.let {
                            ActiveOrderState(
                                id = it.id?.toIntOrNull() ?: 0,
                                customerName = it.customerName ?: "Customer",
                                status = it.status ?: "",
                                amount = it.amount ?: "0"
                            )
                        },
                        upcomingOrders = response.upcomingOrders?.map { order ->
                            UpcomingOrderState(
                                id = order.id?.toIntOrNull() ?: 0,
                                customerName = order.customerName ?: "Customer",
                                status = order.status ?: "",
                                amount = order.amount ?: "0",
                                pickupAddress = order.pickupAddress ?: "",
                                dropAddress = order.dropAddress ?: ""
                            )
                        } ?: emptyList()
                    )
                } else {
                    _error.value = response.message ?: "Failed to load dashboard"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptOrder(orderId: Int) {
        viewModelScope.launch {
            try {
                val response = repository.acceptOrder(orderId, deliveryPartnerId)
                if (response.success) {
                    // Refresh dashboard after accepting
                    loadDashboard()
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

// State classes (should be in a separate file)
data class DeliveryDashboardState(
    val deliveryPartnerName: String,
    val activeOrder: ActiveOrderState?,
    val upcomingOrders: List<UpcomingOrderState>
)

data class ActiveOrderState(
    val id: Int,
    val customerName: String,
    val status: String,
    val amount: String
)

data class UpcomingOrderState(
    val id: Int,
    val customerName: String,
    val status: String,
    val amount: String,
    val pickupAddress: String,
    val dropAddress: String
)





//package com.example.floatingflavors.app.feature.delivery.presentation
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.floatingflavors.app.feature.delivery.data.DeliveryDashboardState
//import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
//import com.example.floatingflavors.app.feature.delivery.data.UpdateOrderStatusRequestDto
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//class DeliveryDashboardViewModel(
//    private val repository: DeliveryRepository,
//    private val deliveryPartnerId: Int
//) : ViewModel() {
//
//    private val _state = MutableStateFlow<DeliveryDashboardState?>(null)
//    val state: StateFlow<DeliveryDashboardState?> = _state
//
//    fun loadDashboard() {
//        viewModelScope.launch {
//            _state.value = repository.loadDashboard(deliveryPartnerId)
//        }
//    }
//
//    fun acceptOrder(orderId: Int) {
//        viewModelScope.launch {
//            repository.updateOrderStatus(
//                UpdateOrderStatusRequestDto(
//                    order_id = orderId,
//                    status = "OUT_FOR_DELIVERY",
//                    delivery_partner_id = deliveryPartnerId
//                )
//            )
//            loadDashboard()
//        }
//    }
//}
