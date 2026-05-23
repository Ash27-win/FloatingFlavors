package com.example.floatingflavors.app.feature.delivery.domain

import org.osmdroid.util.GeoPoint

/**
 * Enterprise Smart Reroute Cooldown Engine
 *
 * Prevents OpenRouteService API spam by enforcing three production-grade guards:
 *  1. Deviation must be strictly > 300 meters from any route segment.
 *  2. The rider must have been continuously off-route for > 20 seconds.
 *  3. A minimum of 45 seconds must have elapsed since the last successful reroute.
 *
 * Without this engine, a single GPS spike could trigger 10+ API calls per second,
 * draining the ORS quota and triggering rate-limit bans.
 */
class RerouteCooldownEngine {

    companion object {
        private const val DEVIATION_THRESHOLD_METERS = 300.0
        private const val CONTINUOUS_DEVIATION_MS    = 20_000L   // 20 seconds
        private const val REROUTE_COOLDOWN_MS        = 45_000L   // 45 seconds
    }

    // Timestamp when the rider FIRST crossed the 300m deviation threshold
    private var deviationStartMs: Long = 0L

    // Timestamp when the last successful reroute was triggered
    private var lastRerouteMs: Long = 0L

    // Whether the rider is currently in a deviation state
    private var isDeviating: Boolean = false

    /**
     * Call this on every GPS update.
     *
     * @param currentLocation  The snapped/kalman-filtered rider position.
     * @param routePoints      A flat list of all GeoPoints from the active route segments.
     * @return true if a reroute should be triggered right now, false otherwise.
     */
    fun shouldReroute(currentLocation: GeoPoint, routePoints: List<GeoPoint>): Boolean {
        val now = System.currentTimeMillis()

        // 1. Calculate minimum distance from rider to the route polyline
        val minDistance = routePoints.minOfOrNull { routePoint ->
            currentLocation.distanceToAsDouble(routePoint)
        } ?: 0.0

        val currentlyDeviating = minDistance > DEVIATION_THRESHOLD_METERS

        // 2. Track continuous deviation window
        if (currentlyDeviating) {
            if (!isDeviating) {
                // First tick of this new deviation event — record start time
                deviationStartMs = now
                isDeviating = true
            }
        } else {
            // Back on route — reset the window
            isDeviating = false
            deviationStartMs = 0L
            return false
        }

        // 3. Guard: must have been continuously deviating for > 20 seconds
        val continuousDeviationMs = now - deviationStartMs
        if (continuousDeviationMs < CONTINUOUS_DEVIATION_MS) return false

        // 4. Guard: enforce 45-second reroute cooldown
        val timeSinceLastReroute = now - lastRerouteMs
        if (timeSinceLastReroute < REROUTE_COOLDOWN_MS) return false

        // All 3 guards passed — trigger reroute
        lastRerouteMs = now
        isDeviating = false   // Reset state after reroute is triggered
        deviationStartMs = 0L
        return true
    }

    /**
     * Resets all state. Call when a new order starts or the user manually selects a route.
     */
    fun reset() {
        deviationStartMs = 0L
        lastRerouteMs    = 0L
        isDeviating      = false
    }

    /**
     * Returns the current deviation distance in meters (for debug overlays).
     */
    fun getDeviationStatus(currentLocation: GeoPoint, routePoints: List<GeoPoint>): Double {
        return routePoints.minOfOrNull { currentLocation.distanceToAsDouble(it) } ?: 0.0
    }
}
