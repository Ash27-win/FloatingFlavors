package com.example.floatingflavors.app.feature.delivery.presentation

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import kotlin.math.*

object SmoothMarkerAnimator {

    private val animators = java.util.WeakHashMap<Marker, ValueAnimator>()

    fun animate(marker: Marker, from: GeoPoint, to: GeoPoint, fromRotation: Float, toRotation: Float, map: org.osmdroid.views.MapView) {
        // 1. Cancel existing animation
        animators[marker]?.cancel()

        val startLat = from.latitude
        val startLng = from.longitude
        val endLat = to.latitude
        val endLng = to.longitude

        // Handle 359->1 degree crossover for rotation
        var diff = toRotation - fromRotation
        if (diff > 180) diff -= 360
        if (diff < -180) diff += 360
        val targetRotation = fromRotation + diff

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 1000 // 1s sync with GPS
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction

            // Position Interp
            val lat = startLat + (endLat - startLat) * fraction
            val lng = startLng + (endLng - startLng) * fraction
            
            // Rotation Interp
            val rot = fromRotation + (diff * fraction)

            marker.position = GeoPoint(lat, lng)
            marker.rotation = rot
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER) // Ensure center rotation
            
            map.invalidate() 
        }

        animator.start()
        animators[marker] = animator
    }
}
