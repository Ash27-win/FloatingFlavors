package com.example.floatingflavors.app.feature.delivery.domain

import org.osmdroid.util.GeoPoint

/**
 * Enterprise-grade GPS smoothing algorithm (Simplified Kalman Filter).
 * 
 * Raw GPS from hardware (especially in cities) bounces due to multipath errors.
 * If we render raw GPS directly, the delivery partner's marker will jump through buildings,
 * and the app will falsely trigger "Route Deviation" recalculations.
 *
 * This filter mathematically averages velocity and hardware noise to produce a smooth,
 * predictable trajectory along the actual road.
 */
class KalmanLocationFilter(private val processNoise: Double = 0.125) {
    private var lastValidPoint: GeoPoint? = null
    private var variance: Double = -1.0

    /**
     * @param lat Raw Hardware Latitude
     * @param lng Raw Hardware Longitude
     * @param accuracy Raw Hardware GPS Accuracy in meters (e.g., from Location.getAccuracy())
     * @return Smoothed GeoPoint safe for UI rendering and Routing Engine
     */
    fun processLocation(lat: Double, lng: Double, accuracy: Double): GeoPoint {
        if (lastValidPoint == null) {
            // Initialization
            lastValidPoint = GeoPoint(lat, lng)
            variance = accuracy * accuracy
            return lastValidPoint!!
        }

        // Kalman prediction step
        val predictedVariance = variance + processNoise

        // Kalman update step
        val measurementVariance = accuracy * accuracy
        val kalmanGain = predictedVariance / (predictedVariance + measurementVariance)

        val smoothedLat = lastValidPoint!!.latitude + kalmanGain * (lat - lastValidPoint!!.latitude)
        val smoothedLng = lastValidPoint!!.longitude + kalmanGain * (lng - lastValidPoint!!.longitude)

        // Update state
        variance = (1 - kalmanGain) * predictedVariance
        val newPoint = GeoPoint(smoothedLat, smoothedLng)
        
        lastValidPoint = newPoint
        return newPoint
    }
}
