package com.example.floatingflavors.app.feature.delivery.domain

import android.util.Log
import com.example.floatingflavors.app.feature.delivery.data.local.DeliveryAnalyticsDao
import com.example.floatingflavors.app.feature.delivery.data.local.DeliveryAnalyticsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

/**
 * Enterprise Delivery Analytics Engine
 *
 * Silently captures operational telemetry during every delivery:
 *  - Detects delay hotspots (speed < 5 km/h for > 60 seconds)
 *  - Tracks average and peak speeds across the whole delivery session
 *  - Records deviation and reroute events
 *
 * Data is batched and uploaded to the PHP backend via [uploadPendingAnalytics]
 * when the order is marked complete.
 *
 * This powers future AI-based rider scoring, ETA calibration, and city
 * congestion heatmaps for the dispatcher dashboard.
 */
class DeliveryAnalyticsEngine(
    private val dao: DeliveryAnalyticsDao,
    private val orderId: Int,
    private val riderId: Int
) {
    companion object {
        private const val TAG = "AnalyticsEngine"
        private const val HOTSPOT_SPEED_KMH   = 5.0    // Below this → potential hotspot
        private const val HOTSPOT_DURATION_MS = 60_000L // Must be slow for 60s to log
    }

    private val scope = CoroutineScope(Dispatchers.IO + Job())

    // Speed tracking
    private var speedHistory        = mutableListOf<Double>()
    private var peakSpeedKmh        = 0.0
    private var totalDistanceKm     = 0.0

    // Hotspot detection
    private var slowStartMs: Long   = 0L
    private var slowStartPoint: GeoPoint? = null
    private var isCurrentlySlow     = false

    // Event counters
    private var deviationCount      = 0
    private var rerouteCount        = 0

    /**
     * Call on every GPS tick with the current speed and position.
     */
    fun onLocationUpdate(speedKmh: Double, location: GeoPoint, distanceDeltaKm: Double) {
        speedHistory.add(speedKmh)
        if (speedKmh > peakSpeedKmh) peakSpeedKmh = speedKmh
        totalDistanceKm += distanceDeltaKm

        detectHotspot(speedKmh, location)
    }

    /** Call whenever the deviation flag is set to true. */
    fun onDeviationDetected() { deviationCount++ }

    /** Call whenever the SmartRerouteCooldown triggers a reroute. */
    fun onRerouteTriggered() { rerouteCount++ }

    /**
     * Detects if the rider has been stationary/slow for > 60 seconds.
     * If so, logs the position as a delay hotspot in Room.
     */
    private fun detectHotspot(speedKmh: Double, location: GeoPoint) {
        val now = System.currentTimeMillis()

        if (speedKmh < HOTSPOT_SPEED_KMH) {
            if (!isCurrentlySlow) {
                isCurrentlySlow  = true
                slowStartMs      = now
                slowStartPoint   = location
            } else {
                val duration = now - slowStartMs
                if (duration >= HOTSPOT_DURATION_MS) {
                    // Log this hotspot and reset the timer to avoid duplicate entries
                    val avgSpeed = if (speedHistory.isEmpty()) 0.0 else speedHistory.average()
                    logHotspot(
                        point         = slowStartPoint ?: location,
                        delaySeconds  = (duration / 1000).toInt(),
                        avgSpeed      = avgSpeed
                    )
                    slowStartMs    = now  // Reset: next tick starts a fresh window
                    slowStartPoint = location
                }
            }
        } else {
            isCurrentlySlow  = false
            slowStartMs      = 0L
            slowStartPoint   = null
        }
    }

    private fun logHotspot(point: GeoPoint, delaySeconds: Int, avgSpeed: Double) {
        scope.launch {
            try {
                dao.insertTelemetry(
                    DeliveryAnalyticsEntity(
                        orderId             = orderId,
                        riderId             = riderId,
                        hotspotLat          = point.latitude,
                        hotspotLng          = point.longitude,
                        delayDurationSeconds = delaySeconds,
                        averageSpeedKmh     = avgSpeed,
                        peakSpeedKmh        = peakSpeedKmh,
                        totalDistanceKm     = totalDistanceKm,
                        hadDeviation        = deviationCount > 0,
                        rerouteCount        = rerouteCount
                    )
                )
                Log.i(TAG, "Hotspot logged at (${point.latitude}, ${point.longitude}) — ${delaySeconds}s delay")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to log hotspot: ${e.message}")
            }
        }
    }

    /**
     * Call when the order is marked COMPLETE.
     * Writes a final summary record and triggers the batch upload.
     */
    fun onDeliveryComplete(uploadCallback: suspend (List<DeliveryAnalyticsEntity>) -> Unit) {
        scope.launch {
            try {
                // Write delivery summary record
                val avgSpeed = if (speedHistory.isEmpty()) 0.0 else speedHistory.average()
                dao.insertTelemetry(
                    DeliveryAnalyticsEntity(
                        orderId              = orderId,
                        riderId              = riderId,
                        hotspotLat           = 0.0,
                        hotspotLng           = 0.0,
                        delayDurationSeconds = 0,
                        averageSpeedKmh      = avgSpeed,
                        peakSpeedKmh         = peakSpeedKmh,
                        totalDistanceKm      = totalDistanceKm,
                        hadDeviation         = deviationCount > 0,
                        rerouteCount         = rerouteCount
                    )
                )

                // Upload all pending records to backend
                val pending = dao.getPendingUploads()
                if (pending.isNotEmpty()) {
                    uploadCallback(pending)
                    dao.markUploaded(pending.map { it.id })
                    Log.i(TAG, "Uploaded ${pending.size} analytics records.")
                }

                // Prune stale uploaded records (older than 7 days)
                dao.pruneUploaded(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000)
            } catch (e: Exception) {
                Log.e(TAG, "Analytics upload failed: ${e.message}")
            }
        }
    }
}
