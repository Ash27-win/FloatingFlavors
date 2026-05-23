package com.example.floatingflavors.app.feature.delivery.presentation.tracking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint

/**
 * Singleton to stream GPS state and connection health from Service → UI.
 * The ViewModel observes these flows without holding a direct Service reference.
 */
object DeliveryLocationStream {

    private val _liveLocation = MutableStateFlow<GeoPoint?>(null)
    val liveLocation = _liveLocation.asStateFlow()

    private val _bearing = MutableStateFlow(0f)
    val bearing = _bearing.asStateFlow()

    // Feature 19: WebSocket connection health
    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    fun updateLocation(lat: Double, lng: Double, bearingVal: Float) {
        _liveLocation.value = GeoPoint(lat, lng)
        _bearing.value = bearingVal
    }

    /** Called by WebSocketHeartbeatEngine when the connection drops or recovers. */
    fun updateConnectionState(isOnline: Boolean) {
        _isOnline.value = isOnline
    }
}
