package com.example.floatingflavors.app.feature.delivery.presentation.tracking

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.osmdroid.util.GeoPoint

/**
 * Singleton to stream location from Service -> UI
 * This ensures UI always shows what the Service sees.
 */
object DeliveryLocationStream {
    private val _liveLocation = MutableStateFlow<GeoPoint?>(null)
    val liveLocation = _liveLocation.asStateFlow()

    private val _bearing = MutableStateFlow(0f)
    val bearing = _bearing.asStateFlow()

    fun updateLocation(lat: Double, lng: Double, bearingVal: Float) {
        _liveLocation.value = GeoPoint(lat, lng)
        _bearing.value = bearingVal
    }
}
