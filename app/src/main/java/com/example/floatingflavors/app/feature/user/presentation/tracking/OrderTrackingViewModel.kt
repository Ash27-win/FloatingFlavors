package com.example.floatingflavors.app.feature.user.presentation.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.user.data.tracking.dto.OrderTrackingResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

class OrderTrackingViewModel : ViewModel() {

    private val _state = MutableStateFlow<OrderTrackingResponse?>(null)
    val state = _state.asStateFlow()

    private val _liveLocation = MutableStateFlow<GeoPoint?>(null)
    val liveLocation = _liveLocation.asStateFlow()

    private val _destination = MutableStateFlow<GeoPoint?>(null)
    val destination = _destination.asStateFlow()


    private var pollingJob: Job? = null

    fun load(orderId: Int, type: String) {
        viewModelScope.launch {
            val response =
                NetworkClient.orderTrackingApi.getOrderTracking(orderId, type)

            _state.value = response

            // ✅ USER DESTINATION (FROM ORDER ADDRESS)
            response.deliveryAddress.let {
                _destination.value = GeoPoint(it.latitude, it.longitude)
            }

            if (response.currentStatus == "OUT_FOR_DELIVERY") {
                startLiveTracking(orderId)
            }
        }
    }

    private fun startLiveTracking(orderId: Int) {
        pollingJob?.cancel()

        pollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val response =
                        NetworkClient.orderTrackingApi.getLiveLocation(orderId)

                    if (response.success && response.location != null) {
                        _liveLocation.value = GeoPoint(
                            response.location.latitude,
                            response.location.longitude
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(3000) // 🔥 REAL TIME
            }
        }
    }

    fun calculateDistance(
        a: GeoPoint,
        b: GeoPoint
    ): Double {
        return a.distanceToAsDouble(b) / 1000 // KM
    }


    override fun onCleared() {
        pollingJob?.cancel()
    }
}


