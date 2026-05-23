package com.example.floatingflavors.app.feature.user.presentation.tracking

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

/**
 * Enterprise Camera Easing Controller
 * 
 * Replaces aggressive map snaps with smooth, cinematic camera panning, zooming, and rotation.
 * Emulates the "Google Maps" navigation feel.
 */
class CameraEasingController(private val map: MapView) {

    private var currentAnimatorSet: AnimatorSet? = null

    /**
     * Smoothly animate the map to the target location, applying bearing and dynamic zoom.
     */
    fun animateCamera(
        targetLocation: GeoPoint,
        targetBearing: Float,
        speedKmh: Double,
        isNavigating: Boolean
    ) {
        currentAnimatorSet?.cancel()

        val startLat = map.mapCenter.latitude
        val startLng = map.mapCenter.longitude
        val startZoom = map.zoomLevelDouble
        val startBearing = map.mapOrientation

        // Dynamic Zoom: Zoom out when driving fast to see further ahead, zoom in when slow/stopped.
        val targetZoom = when {
            speedKmh > 60.0 -> 16.0
            speedKmh > 30.0 -> 17.5
            else -> 19.0
        }

        // Handle bearing wraparound (e.g. 350 degrees to 10 degrees)
        var endBearing = if (isNavigating) -targetBearing else 0f
        val bearingDiff = endBearing - startBearing
        if (bearingDiff > 180) endBearing -= 360
        if (bearingDiff < -180) endBearing += 360

        val panAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1000L
            addUpdateListener { animator ->
                val fraction = animator.animatedFraction
                val lat = startLat + (targetLocation.latitude - startLat) * fraction
                val lng = startLng + (targetLocation.longitude - startLng) * fraction
                map.controller.setCenter(GeoPoint(lat, lng))
            }
        }

        val zoomAnimator = ValueAnimator.ofFloat(startZoom.toFloat(), targetZoom.toFloat()).apply {
            duration = 1000L
            addUpdateListener { animator ->
                map.controller.setZoom((animator.animatedValue as Float).toDouble())
            }
        }

        val bearingAnimator = ValueAnimator.ofFloat(startBearing, endBearing).apply {
            duration = 1000L
            addUpdateListener { animator ->
                map.mapOrientation = animator.animatedValue as Float
            }
        }

        currentAnimatorSet = AnimatorSet().apply {
            playTogether(panAnimator, zoomAnimator, bearingAnimator)
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    fun dispose() {
        currentAnimatorSet?.cancel()
        currentAnimatorSet = null
    }
}
