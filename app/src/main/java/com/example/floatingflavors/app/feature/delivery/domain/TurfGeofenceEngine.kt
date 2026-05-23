package com.example.floatingflavors.app.feature.delivery.domain

import org.osmdroid.util.GeoPoint

/**
 * Enterprise Polygon Geofence Engine
 * 
 * Standard circular geofences trigger falsely when a rider is on a highway next to the hub.
 * This engine uses Ray-Casting algorithms to check if the rider is strictly inside the 
 * exact boundaries of the Cloud Kitchen parking lot.
 */
object TurfGeofenceEngine {

    // Define the exact corners of the Floating Flavors Cloud Kitchen Hub
    private val hubPolygon = listOf(
        GeoPoint(12.551800, 80.163200), // Top Left
        GeoPoint(12.551800, 80.163500), // Top Right
        GeoPoint(12.551500, 80.163500), // Bottom Right
        GeoPoint(12.551500, 80.163200)  // Bottom Left
    )

    /**
     * Ray-Casting algorithm to determine if a point is inside a polygon.
     */
    fun isInsideHubGeofence(point: GeoPoint): Boolean {
        var isInside = false
        val n = hubPolygon.size
        var j = n - 1

        for (i in 0 until n) {
            val pi = hubPolygon[i]
            val pj = hubPolygon[j]

            val intersect = ((pi.longitude > point.longitude) != (pj.longitude > point.longitude)) &&
                    (point.latitude < (pj.latitude - pi.latitude) * (point.longitude - pi.longitude) / (pj.longitude - pi.longitude) + pi.latitude)
            if (intersect) isInside = !isInside
            j = i
        }

        return isInside
    }
}
