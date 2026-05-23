package com.example.floatingflavors.app.feature.delivery.domain

import android.util.Log

/**
 * Dynamic Traffic Confidence System
 *
 * Problem: The app reroutes riders into alleyways based on traffic data that is
 * 30+ minutes old from OpenRouteService, causing worse journey times.
 *
 * Solution:
 *  - Attach a timestamp to every ORS traffic response.
 *  - If traffic data is FRESH (< 5 minutes old): full confidence, render colored segments.
 *  - If traffic data is STALE (5–15 minutes old): degraded confidence, show warning color.
 *  - If traffic data is EXPIRED (> 15 minutes old): zero confidence, revert all polylines
 *    to standard Blue and disable automatic "Fastest Route" switching entirely.
 *
 * This prevents silent trust in stale data, which is one of the most common causes
 * of "the app sent me the wrong way" complaints in delivery apps.
 */
object TrafficConfidenceEngine {

    private const val TAG = "TrafficConfidence"

    /** Data fresher than 5 minutes → full confidence */
    private const val FRESH_THRESHOLD_MS     = 5L  * 60 * 1000  // 5 min

    /** Data between 5–15 minutes → degraded confidence, show warning */
    private const val DEGRADED_THRESHOLD_MS  = 15L * 60 * 1000  // 15 min

    // Timestamp of the last ORS traffic response received
    private var lastTrafficFetchMs: Long = 0L

    // Whether "Fastest Route" auto-switching is currently permitted
    var isFastestRouteSwitchingEnabled: Boolean = true
        private set

    /**
     * Call every time a fresh ORS route response is received to reset the clock.
     */
    fun onTrafficDataReceived() {
        lastTrafficFetchMs = System.currentTimeMillis()
        isFastestRouteSwitchingEnabled = true
        Log.i(TAG, "Traffic data refreshed at ${lastTrafficFetchMs}ms")
    }

    /**
     * Returns the current traffic confidence level.
     * The UI uses this to determine polyline colors and warning banners.
     */
    fun getConfidenceLevel(): TrafficConfidence {
        if (lastTrafficFetchMs == 0L) return TrafficConfidence.NO_DATA

        val age = System.currentTimeMillis() - lastTrafficFetchMs
        return when {
            age < FRESH_THRESHOLD_MS    -> TrafficConfidence.FRESH
            age < DEGRADED_THRESHOLD_MS -> {
                Log.w(TAG, "Traffic data is ${age / 60000}m old — degraded confidence.")
                TrafficConfidence.STALE
            }
            else -> {
                // Lock out Fastest Route switching when data is expired
                isFastestRouteSwitchingEnabled = false
                Log.e(TAG, "Traffic data EXPIRED (${age / 60000}m old). Fastest Route disabled.")
                TrafficConfidence.EXPIRED
            }
        }
    }

    /**
     * Returns the polyline color to use based on the current confidence level.
     *
     * FRESH   → Traffic-aware color (red/yellow/green from ORS annotations)
     * STALE   → Amber warning color
     * EXPIRED → Standard blue (ignore traffic entirely)
     * NO_DATA → Standard blue
     */
    fun getPolylineColor(orsTrafficColor: Long): Long {
        return when (getConfidenceLevel()) {
            TrafficConfidence.FRESH   -> orsTrafficColor
            TrafficConfidence.STALE   -> 0xFFFFA000L  // Amber
            TrafficConfidence.EXPIRED,
            TrafficConfidence.NO_DATA -> 0xFF1E88E5L  // Standard blue
        }
    }

    /**
     * Returns a UI banner message to warn the rider about stale traffic data.
     * Returns null if traffic data is fresh and no banner is needed.
     */
    fun getTrafficWarningBanner(): String? {
        return when (getConfidenceLevel()) {
            TrafficConfidence.FRESH   -> null
            TrafficConfidence.STALE   -> "⚠ Traffic data may be outdated"
            TrafficConfidence.EXPIRED -> "⚠ No live traffic — using standard route"
            TrafficConfidence.NO_DATA -> null
        }
    }

    /** Returns the age of the current traffic data in minutes (for debug overlays). */
    fun getTrafficDataAgeMinutes(): Long {
        if (lastTrafficFetchMs == 0L) return -1L
        return (System.currentTimeMillis() - lastTrafficFetchMs) / 60_000L
    }

    fun reset() {
        lastTrafficFetchMs = 0L
        isFastestRouteSwitchingEnabled = true
    }
}

enum class TrafficConfidence {
    FRESH,    // < 5 minutes old
    STALE,    // 5–15 minutes old
    EXPIRED,  // > 15 minutes old
    NO_DATA   // Never received
}
