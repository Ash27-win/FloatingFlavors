package com.example.floatingflavors.app.feature.delivery.domain

import com.example.floatingflavors.app.feature.user.presentation.tracking.NavigationRoute
import org.osmdroid.util.GeoPoint
import kotlin.math.*

/**
 * Enterprise Navigation Snap-To-Road Engine.
 *
 * Mathematically projects a GPS coordinate onto the nearest segment of the active route.
 * Prevents the delivery marker from visually drifting off the road into buildings or water.
 */
object TurfSnapEngine {

    /**
     * Finds the closest projected point on the active route.
     * If the distance is > 50 meters, we assume they are crossing a parking lot
     * and return the original point to avoid snapping them backwards artificially.
     */
    fun snapToRoute(currentLocation: GeoPoint, activeRoute: NavigationRoute?): GeoPoint {
        if (activeRoute == null) return currentLocation

        var closestPoint = currentLocation
        var minDistance = Double.MAX_VALUE

        activeRoute.segments.forEach { segment ->
            for (i in 0 until segment.points.size - 1) {
                val start = segment.points[i]
                val end = segment.points[i + 1]

                val projected = projectPointOnLine(currentLocation, start, end)
                val dist = currentLocation.distanceToAsDouble(projected)

                if (dist < minDistance) {
                    minDistance = dist
                    closestPoint = projected
                }
            }
        }

        // Only snap if we are reasonably close to the road (e.g. < 50 meters).
        // If > 50 meters, they might be in a large driveway/apartment complex.
        return if (minDistance < 50.0) closestPoint else currentLocation
    }

    private fun projectPointOnLine(p: GeoPoint, a: GeoPoint, b: GeoPoint): GeoPoint {
        val aLat = Math.toRadians(a.latitude)
        val aLng = Math.toRadians(a.longitude)
        val bLat = Math.toRadians(b.latitude)
        val bLng = Math.toRadians(b.longitude)
        val pLat = Math.toRadians(p.latitude)
        val pLng = Math.toRadians(p.longitude)

        val dx = bLng - aLng
        val dy = bLat - aLat

        // If a == b, just return a
        if (dx == 0.0 && dy == 0.0) return a

        val t = ((pLng - aLng) * dx + (pLat - aLat) * dy) / (dx * dx + dy * dy)
        
        // Clamp to segment
        val clampedT = max(0.0, min(1.0, t))

        val projLat = Math.toDegrees(aLat + clampedT * dy)
        val projLng = Math.toDegrees(aLng + clampedT * dx)

        return GeoPoint(projLat, projLng)
    }
    
    /**
     * Calculates the bearing (angle) from point A to point B for camera rotation.
     */
    fun calculateBearing(start: GeoPoint, end: GeoPoint): Float {
        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)
        val lat2 = Math.toRadians(end.latitude)
        val lon2 = Math.toRadians(end.longitude)

        val dLon = lon2 - lon1
        val y = sin(dLon) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
        
        var bearing = Math.toDegrees(atan2(y, x)).toFloat()
        bearing = (bearing + 360) % 360
        return bearing
    }
}
