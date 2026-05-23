package com.example.floatingflavors.app.feature.user.presentation.tracking

import android.animation.ValueAnimator
import android.graphics.Color
import android.view.animation.LinearInterpolator
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/**
 * Enterprise Map Render Controller — with Polyline Diff Engine (Feature 16)
 *
 * Previous implementation: On every GPS update, nuke ALL polylines from overlays
 * and re-draw the full route from scratch. On a 200-segment route, this is 200
 * object allocations + 200 canvas draws every second = GPU jank, frame drops, OOM.
 *
 * Polyline Diff Engine:
 *  - Maintains a registry of rendered segments keyed by [segmentKey] = "routeId:segmentIndex"
 *  - On each [updateRoutes] call, computes a diff between the previous set and the new set.
 *  - Only REMOVES segments that are no longer present.
 *  - Only ADDS segments that are genuinely new.
 *  - UPDATES color/width on existing segments in-place without removing and re-adding.
 *
 * Result: For a 200-segment route, a typical GPS update now touches only 1–3 segments
 * (the rider has passed a junction and the active color changed), not all 200.
 */
class MapRenderController(private val map: MapView) {

    private var marker: Marker? = null
    private var destMarker: Marker? = null
    private var hubMarker: Marker? = null
    private val cameraEasingController = CameraEasingController(map)

    // Animator for marker movement
    private var markerAnimator: ValueAnimator? = null

    // ── Polyline Diff Registry ────────────────────────────────────────────────
    // key = "routeId:segmentIndex" → rendered Polyline
    private val polylineRegistry = mutableMapOf<String, Polyline>()
    
    // ── ETA Marker Registry ───────────────────────────────────────────────────
    private val etaMarkerRegistry = mutableMapOf<String, Marker>()

    // ── Utilities ─────────────────────────────────────────────────────────────
    private fun createRiderDrawable(context: android.content.Context): android.graphics.drawable.Drawable {
        val scale = context.resources.displayMetrics.density
        val size = (40 * scale).toInt()
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        
        // Draw blue glow (outer)
        paint.color = Color.parseColor("#442E63F5") // Translucent blue
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        
        // Draw orange inner circle
        paint.color = Color.parseColor("#FF6D00") // Premium Orange
        val innerRadius = size / 3.5f
        canvas.drawCircle(size / 2f, size / 2f, innerRadius, paint)
        
        // Draw scooter icon
        val d = androidx.core.content.ContextCompat.getDrawable(context, com.example.floatingflavors.R.drawable.ic_marker_scooter)
        if (d != null) {
            val iconSize = (14 * scale).toInt()
            val offset = (size - iconSize) / 2
            d.setBounds(offset, offset, offset + iconSize, offset + iconSize)
            // Tint icon black to match design
            d.setTint(Color.BLACK)
            d.draw(canvas)
        }
        return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
    }

    private fun createHubDrawable(context: android.content.Context): android.graphics.drawable.Drawable {
        val scale = context.resources.displayMetrics.density
        val size = (28 * scale).toInt()
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        
        // Draw downward pin (triangle)
        paint.color = Color.parseColor("#E65100") // Darker orange for pin
        val path = android.graphics.Path()
        path.moveTo(size / 2f, size.toFloat()) // Bottom tip
        path.lineTo(size / 2f - 7 * scale, size / 2f)
        path.lineTo(size / 2f + 7 * scale, size / 2f)
        path.close()
        canvas.drawPath(path, paint)
        
        // Draw orange circle
        paint.color = Color.parseColor("#FF6D00") // Premium Orange
        canvas.drawCircle(size / 2f, size / 2.5f, 12 * scale, paint)
        
        // Draw "FF"
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        textPaint.color = Color.WHITE
        textPaint.textSize = 10 * scale
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        // Adjust Y for text centering
        canvas.drawText("FF", size / 2f, size / 2.5f + (3.5f * scale), textPaint)
        
        return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
    }

    private fun createEtaChipDrawable(context: android.content.Context, label: String, etaMin: Int, distanceKm: Double, isActive: Boolean): android.graphics.drawable.Drawable {
        val scale = context.resources.displayMetrics.density
        val width = (100 * scale).toInt()
        val height = (55 * scale).toInt()
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        
        // Background
        paint.color = if (isActive) Color.parseColor("#2E63F5") else Color.WHITE
        val rect = android.graphics.RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rect, 12 * scale, 12 * scale, paint)
        
        if (!isActive) {
            paint.style = android.graphics.Paint.Style.STROKE
            paint.color = Color.parseColor("#E5E7EB")
            paint.strokeWidth = 2 * scale
            canvas.drawRoundRect(rect, 12 * scale, 12 * scale, paint)
            paint.style = android.graphics.Paint.Style.FILL
        }
        
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        textPaint.textAlign = android.graphics.Paint.Align.CENTER
        
        // Label
        textPaint.textSize = 10 * scale
        textPaint.color = if (isActive) Color.parseColor("#DBEAFE") else Color.parseColor("#64748B")
        canvas.drawText(label, width / 2f, 16 * scale, textPaint)
        
        // ETA
        textPaint.textSize = 16 * scale
        textPaint.typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textPaint.color = if (isActive) Color.WHITE else Color.BLACK
        canvas.drawText("$etaMin min", width / 2f, 36 * scale, textPaint)
        
        // Distance
        textPaint.textSize = 11 * scale
        textPaint.typeface = android.graphics.Typeface.DEFAULT
        textPaint.color = if (isActive) Color.parseColor("#DBEAFE") else Color.parseColor("#64748B")
        canvas.drawText("${String.format("%.1f", distanceKm)} km", width / 2f, 48 * scale, textPaint)
        
        return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
    }

    // ── Marker Update ─────────────────────────────────────────────────────────

    fun updateMarkerPosition(newPoint: GeoPoint, bearing: Float, speedKmh: Double, isNavigating: Boolean) {
        if (marker == null) {
            marker = Marker(map).apply {
                position = newPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                rotation = if (isNavigating) -bearing else 0f
                // Custom Glowing Bike Icon
                icon = createRiderDrawable(map.context)
                title = "You"
            }
            map.overlays.add(marker)
            cameraEasingController.animateCamera(newPoint, bearing, speedKmh, isNavigating)
        } else {
            val startPoint = marker!!.position

            if (startPoint.latitude != newPoint.latitude || startPoint.longitude != newPoint.longitude) {
                markerAnimator?.cancel()
                markerAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 1000L
                    interpolator = LinearInterpolator()
                    addUpdateListener { animator ->
                        val fraction = animator.animatedFraction
                        val lat = (newPoint.latitude  - startPoint.latitude)  * fraction + startPoint.latitude
                        val lng = (newPoint.longitude - startPoint.longitude) * fraction + startPoint.longitude

                        marker?.position = GeoPoint(lat, lng)

                        marker?.let { currentMarker ->
                            val startRot = currentMarker.rotation
                            var endRot = if (isNavigating) -bearing else 0f
                            val diff = endRot - startRot
                            if (diff >  180) endRot -= 360
                            if (diff < -180) endRot += 360
                            currentMarker.rotation = startRot + (endRot - startRot) * fraction
                        }

                        map.invalidate()
                    }
                    start()
                }
                cameraEasingController.animateCamera(newPoint, bearing, speedKmh, isNavigating)
            }
        }
    }

    // ── Polyline Diff Engine ──────────────────────────────────────────────────

    /**
     * Efficiently updates the map's polylines using a diff algorithm.
     *
     * Instead of clearing all overlays and re-drawing everything, this method:
     *  1. Builds the "desired" set of segment keys from the incoming [routes].
     *  2. Removes only polylines whose keys are no longer in the desired set.
     *  3. Adds only polylines that are genuinely new.
     *  4. Updates color in-place on existing segments (e.g. traffic color change).
     */
    fun updateRoutes(
        routes: List<NavigationRoute>,
        activeRouteId: String?,
        onRouteSelected: ((String) -> Unit)?,
        isNavigating: Boolean
    ) {
        val desiredKeys = mutableSetOf<String>()

        // ── 1. Build desired state ────────────────────────────────────────────
        routes.forEachIndexed { routeIndex, route ->
            val isActive = route.id == activeRouteId
            route.segments.forEachIndexed { segIndex, segment ->
                val key   = "${route.id}:$segIndex"
                
                val targetColor = if (isActive) {
                    if (isNavigating) segment.color else Color.parseColor("#2E63F5")
                } else {
                    if (routeIndex == 1) Color.parseColor("#757575") else Color.parseColor("#BDBDBD")
                }
                val width = if (isActive) 16f else 10f

                desiredKeys.add(key)

                val existing = polylineRegistry[key]
                if (existing == null) {
                    // ── NEW segment: create and add ───────────────────────────
                    val line = Polyline(map).apply {
                        outlinePaint.color       = targetColor
                        outlinePaint.strokeWidth  = width
                        if (!isActive) {
                            setOnClickListener { _, _, _ ->
                                onRouteSelected?.invoke(route.id)
                                true
                            }
                        }
                    }
                    line.setPoints(segment.points)
                    polylineRegistry[key] = line

                    // Insert alt routes behind active route (z-order: alt at 0, active on top)
                    val insertIndex = if (isActive) map.overlays.size else 0
                    map.overlays.add(insertIndex.coerceIn(0, map.overlays.size), line)

                } else {
                    // ── EXISTING segment: diff-update paint only if changed ───
                    if (existing.outlinePaint.color != targetColor) {
                        existing.outlinePaint.color = targetColor
                    }
                    if (existing.outlinePaint.strokeWidth != width) {
                        existing.outlinePaint.strokeWidth = width
                    }
                }
            }
        }

        // ── 2. Remove segments no longer in the desired set ───────────────────
        val keysToRemove = polylineRegistry.keys.filter { it !in desiredKeys }
        keysToRemove.forEach { key ->
            val line = polylineRegistry.remove(key)
            if (line != null) map.overlays.remove(line)
        }

        // ── Removed ETA Chips Management in favor of Compose Overlay ──
        
        // ── 3. Update Destination and Hub Markers ──────────────────────────────
        // We will just recreate them if they don't exist yet, placing them at start/end of active route
        val activeRoute = routes.find { it.id == activeRouteId } ?: routes.firstOrNull()
        if (activeRoute != null && activeRoute.segments.isNotEmpty()) {
            val lastSegment = activeRoute.segments.last()
            val destPoint = lastSegment.points.lastOrNull()
            
            if (destPoint != null) {
                if (destMarker == null) {
                    destMarker = Marker(map).apply {
                        position = destPoint
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = androidx.core.content.ContextCompat.getDrawable(map.context, com.example.floatingflavors.R.drawable.ic_location_pin)
                        title = "Destination"
                    }
                    map.overlays.add(destMarker)
                } else {
                    destMarker!!.position = destPoint
                }
            }
            
            // Hub Marker (Constant)
            val hubPoint = GeoPoint(12.5186, 80.1555) // Kalpakkam Hub
            if (hubMarker == null) {
                hubMarker = Marker(map).apply {
                    position = hubPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = createHubDrawable(map.context)
                    title = "Floating Flavors Hub"
                }
                map.overlays.add(hubMarker)
            }
        }

        map.invalidate()
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    fun dispose() {
        markerAnimator?.cancel()
        markerAnimator = null
        cameraEasingController.dispose()
        marker?.let { map.overlays.remove(it) }
        destMarker?.let { map.overlays.remove(it) }
        hubMarker?.let { map.overlays.remove(it) }
        etaMarkerRegistry.values.forEach { map.overlays.remove(it) }
        etaMarkerRegistry.clear()
        polylineRegistry.values.forEach { map.overlays.remove(it) }
        polylineRegistry.clear()
        marker = null
        destMarker = null
        hubMarker = null
        map.onDetach()
    }
}
