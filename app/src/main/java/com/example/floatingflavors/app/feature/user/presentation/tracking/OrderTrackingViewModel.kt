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

    private val _routes = MutableStateFlow<List<NavigationRoute>>(emptyList())
    val routes = _routes.asStateFlow()


    private var pollingJob: Job? = null

    fun load(orderId: Int, type: String) {
        viewModelScope.launch {
            val response =
                NetworkClient.orderTrackingApi.getOrderTracking(orderId, type)

            _state.value = response

            // ✅ USER DESTINATION (FROM ORDER ADDRESS)
            response.deliveryAddress.let {
                val lat = it.latitude ?: 0.0
                val lng = it.longitude ?: 0.0
                _destination.value = GeoPoint(lat, lng)
            }

            if (response.currentStatus == "OUT_FOR_DELIVERY") {
                startLiveTracking(orderId)
            }
        }
    }

    private fun fetchRouteToDestination(start: GeoPoint, end: GeoPoint) {
        viewModelScope.launch {
            try {
                val (points, etaMin) = com.example.floatingflavors.app.feature.user.data.tracking.OsrmRouteService.fetchRouteWithEta(start, end)
                if (points.isNotEmpty()) {
                    val distKm = start.distanceToAsDouble(end) / 1000.0
                    val segment = RouteSegment(points = points, color = android.graphics.Color.BLUE)
                    val navRoute = NavigationRoute(
                        id = "user_route",
                        segments = listOf(segment),
                        totalDistanceKm = distKm,
                        totalEtaMinutes = etaMin,
                        isFastest = true
                    )
                    _routes.value = listOf(navRoute)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
                        val currentGeo = GeoPoint(
                            response.location.latitude,
                            response.location.longitude
                        )
                        _liveLocation.value = currentGeo
                        
                        // Update Route once we have both locations and haven't fetched it yet
                        if (_routes.value.isEmpty() && _destination.value != null) {
                            fetchRouteToDestination(currentGeo, _destination.value!!)
                        }
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


