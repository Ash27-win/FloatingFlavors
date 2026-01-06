package com.example.floatingflavors.app.feature.delivery.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryOrderData // ✅ Use from DeliveryApi.kt
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryOrderDetailsResponse // ✅ Correct import
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationUpdateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class DeliveryOrder(
    val id: String,
    val customerName: String?,
    val status: String?,
    val amount: String?,
    val deliveryPartnerId: String?
)

class DeliveryOrderDetailsViewModel(
    private val orderId: Int,
    private val deliveryPartnerId: Int
) : ViewModel() {

    private val repository = DeliveryRepository()

    private val _order = MutableStateFlow<DeliveryOrder?>(null)
    val order: StateFlow<DeliveryOrder?> = _order

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isTrackingStarted = MutableStateFlow(false)
    val isTrackingStarted: StateFlow<Boolean> = _isTrackingStarted

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadOrderDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // ✅ REAL API CALL - Remove explicit type if causing issues
                val response = repository.getOrderDetails(orderId)

                // Check if response is successful
                if (response.success && response.data != null) {
                    // Map to DeliveryOrder
                    _order.value = DeliveryOrder(
                        id = response.data.id ?: orderId.toString(),
                        customerName = response.data.customerName ?: "Customer",
                        status = response.data.status ?: "UNKNOWN",
                        amount = response.data.amount ?: "0",
                        deliveryPartnerId = response.data.deliveryPartnerId
                    )
                } else {
                    _error.value = response.message ?: "Failed to load order details"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
                e.printStackTrace() // Add this for debugging
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptOrder(context: Context? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // ✅ REAL API CALL
                val response = repository.acceptOrder(orderId, deliveryPartnerId)

                if (response.success) {
                    // Reload order details
                    loadOrderDetails()

                    // ✅ AUTO-START GPS TRACKING after accepting order
                    context?.let { startTracking(it) }
                } else {
                    _error.value = response.message ?: "Failed to accept order"
                }
            } catch (e: Exception) {
                _error.value = "Error accepting order: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectOrder() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // ✅ REAL API CALL
                val response = repository.rejectOrder(orderId)

                if (response.success) {
                    // Reload order details
                    loadOrderDetails()
                } else {
                    _error.value = response.message ?: "Failed to reject order"
                }
            } catch (e: Exception) {
                _error.value = "Error rejecting order: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsDelivered(context: Context? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // ✅ REAL API CALL
                val response = repository.markAsDelivered(orderId)

                if (response.success) {
                    // Reload order details
                    loadOrderDetails()

                    // ✅ STOP GPS TRACKING when delivered
                    context?.let { stopTracking(it) }
                } else {
                    _error.value = response.message ?: "Failed to mark as delivered"
                }
            } catch (e: Exception) {
                _error.value = "Error marking as delivered: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun startTracking(context: Context) {
        // Update UI state
        _isTrackingStarted.value = true

        // ✅ START GPS TRACKING SERVICE
        val intent = Intent(context, DeliveryLocationUpdateService::class.java).apply {
            action = "START_TRACKING"
            putExtra("ORDER_ID", orderId)
            putExtra("DELIVERY_PARTNER_ID", deliveryPartnerId) // 🔥 REQUIRED
        }
        context.startForegroundService(intent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopTracking(context: Context) {
        // Update UI state
        _isTrackingStarted.value = false

        // ✅ STOP GPS TRACKING SERVICE
        val intent = Intent(context, DeliveryLocationUpdateService::class.java).apply {
            action = "STOP_TRACKING"
        }
        context.stopService(intent)
    }

    fun clearError() {
        _error.value = null
    }
}

// Factory for ViewModel
class DeliveryOrderDetailsViewModelFactory(
    private val orderId: Int,
    private val deliveryPartnerId: Int
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DeliveryOrderDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DeliveryOrderDetailsViewModel(orderId, deliveryPartnerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}