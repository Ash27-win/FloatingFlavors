package com.example.floatingflavors.app.feature.delivery.presentation

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.OrderItemDto
import com.example.floatingflavors.app.feature.delivery.presentation.tracking.DeliveryLocationUpdateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.location.Geocoder
import java.util.Locale

/* -------------------- UI MODEL -------------------- */

data class DeliveryOrder(
    val id: String,
    val customerName: String,
    val customerPhone: String? = null,
    val status: String,
    val amount: String,
    val deliveryPartnerId: String?,
    val items: List<OrderItemDto>,
    val distance: String?,
    val createdAt: String?,
    val deliveryAddress: String?
)

/* -------------------- VIEWMODEL -------------------- */

class DeliveryOrderDetailsViewModel(
    private val orderId: Int,
    private val deliveryPartnerId: Int
) : ViewModel() {

    private val repository = DeliveryRepository()

    private val _order = MutableStateFlow<DeliveryOrder?>(null)
    val order: StateFlow<DeliveryOrder?> = _order

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _pickupAddress = MutableStateFlow("Fetching live location...")
    val pickupAddress: StateFlow<String> = _pickupAddress

    // 🔒 PREVENT MULTIPLE INFINITE LOOPS
    private var isObserving = false

    /* -------------------- ORDER DETAILS -------------------- */

    fun loadOrderDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val res = repository.getOrderDetails(orderId)
                if (res.success && res.data != null) {
                    val d = res.data
                    _order.value = DeliveryOrder(
                        id = d.id!!,
                        customerName = d.customerName!!,
                        customerPhone = d.customerPhone,
                        status = d.status!!,
                        amount = d.amount!!,
                        deliveryPartnerId = d.deliveryPartnerId,
                        items = d.items ?: emptyList(),
                        distance = d.distance,
                        createdAt = d.createdAt,
                        deliveryAddress = d.deliveryAddress
                    )
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /* -------------------- LIVE PICKUP OBSERVER -------------------- */

    fun startObservingPickup(context: Context) {
        if (isObserving) return
        isObserving = true
        observeLiveLocation(context)
    }

    private fun observeLiveLocation(context: Context) {
        viewModelScope.launch {
            while (true) {
                val loc = repository.getLastLiveLocation(orderId)
                if (loc != null) {
                    updatePickupAddressFromLocation(
                        context,
                        loc.latitude,
                        loc.longitude
                    )
                }
                kotlinx.coroutines.delay(8000)
            }
        }
    }

    private fun updatePickupAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)

            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val readable = listOfNotNull(
                    addr.subLocality,
                    addr.locality,
                    addr.adminArea,
                    addr.postalCode
                ).joinToString(", ")

                _pickupAddress.value = readable
            }
        } catch (e: Exception) {
            _pickupAddress.value = "Live location active"
        }
    }

    /* -------------------- ACTIONS -------------------- */

    @RequiresApi(Build.VERSION_CODES.O)
    fun acceptOrderAndStartTracking(context: Context) {
        viewModelScope.launch {
            val res = repository.acceptOrder(orderId, deliveryPartnerId)
            if (res.success) {
                loadOrderDetails()
                startTracking(context)
                // ❌ DO NOT START observeLiveLocation() HERE
            }
        }
    }

    fun rejectOrder() {
        viewModelScope.launch {
            repository.rejectOrder(orderId)
            loadOrderDetails()
        }
    }

    fun markAsDelivered(context: Context) {
        viewModelScope.launch {
            repository.markAsDelivered(orderId)
            stopTracking(context)
            isObserving = false
            loadOrderDetails()
        }
    }

    /* -------------------- TRACKING SERVICE -------------------- */

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTracking(context: Context) {
        val intent = Intent(context, DeliveryLocationUpdateService::class.java).apply {
            action = "START_TRACKING"
            putExtra("ORDER_ID", orderId)
            putExtra("DELIVERY_PARTNER_ID", deliveryPartnerId)
        }
        context.startForegroundService(intent)
    }

    private fun stopTracking(context: Context) {
        context.stopService(Intent(context, DeliveryLocationUpdateService::class.java))
    }
}
