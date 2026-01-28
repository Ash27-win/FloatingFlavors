package com.example.floatingflavors.app.feature.delivery.presentation

import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun DeliveryTrackingMap(
    livePoint: GeoPoint?,
    destination: GeoPoint?,
    routes: List<List<GeoPoint>>, // List of all routes
    selectedRouteIndex: Int,
    isNavigationStarted: Boolean,
    bearing: Float,        // GPS Bearing
    compassBearing: Float, // Device Compass Bearing
    modifier: Modifier = Modifier
) {
    // if (livePoint == null) return  <-- REMOVED to keep MapView visible


    // State for overlays to prevent recreation
    val traveledPolyline = remember { 
        Polyline().apply {
            outlinePaint.color = 0xFFCCCCCC.toInt() // Gray
            outlinePaint.strokeWidth = 20f
            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
        } 
    }
    val remainingPolyline = remember { 
        Polyline().apply {
            outlinePaint.color = 0xFF2E63F5.toInt() // Brand Blue
            outlinePaint.strokeWidth = 20f
            outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
        } 
    }
    
    // Markers reuse
    var startMarker by remember { mutableStateOf<Marker?>(null) }
    var destMarker by remember { mutableStateOf<Marker?>(null) }
    var liveMarker by remember { mutableStateOf<Marker?>(null) }
    
    // Initial Center Logi
    var isMapInitialized by remember { mutableStateOf(false) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val config = Configuration.getInstance()
            config.load(context, context.getSharedPreferences("osm", Context.MODE_PRIVATE))
            
            val osmCache = java.io.File(context.cacheDir, "osmdroid")
            config.osmdroidBasePath = osmCache
            config.osmdroidTileCache = osmCache
            config.userAgentValue = "FloatingFlavorsApp/1.0"

            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                isTilesScaledToDpi = true
                
                overlays.add(traveledPolyline)
                overlays.add(remainingPolyline)
                
                controller.setZoom(18.0)
            }
        },
        update = { map ->
            
            // 0. INITIAL CAMERA FIX (Fix Blue Screen)
            if (!isMapInitialized) {
                if (livePoint != null) {
                    map.controller.setCenter(livePoint)
                    isMapInitialized = true
                } else if (destination != null) {
                    map.controller.setCenter(destination)
                    isMapInitialized = true
                } else if (routes.isNotEmpty() && routes[0].isNotEmpty()) {
                    map.controller.setCenter(routes[0].first())
                    isMapInitialized = true
                }
            }

            // 1. DRAW ROUTES
            if (routes.isNotEmpty()) {
                val currentRoute = routes.getOrNull(selectedRouteIndex) ?: emptyList()
                if (currentRoute.isNotEmpty()) {
                    if (livePoint != null) {
                        val (traveled, remaining) = splitRouteAtPoint(currentRoute, livePoint)
                        traveledPolyline.setPoints(traveled)
                        remainingPolyline.setPoints(remaining)
                    } else {
                        remainingPolyline.setPoints(currentRoute)
                        traveledPolyline.setPoints(emptyList())
                    }
                    if (!map.overlays.contains(traveledPolyline)) map.overlays.add(0, traveledPolyline)
                    if (!map.overlays.contains(remainingPolyline)) map.overlays.add(0, remainingPolyline)
                }
            }

            // 2. MARKERS
            
            // A) Start
            if (routes.isNotEmpty() && routes[0].isNotEmpty()) {
                val startPoint = routes[0].first()
                if (startMarker == null) {
                    startMarker = Marker(map).apply {
                        position = startPoint
                        title = "Restaurant"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = androidx.core.content.ContextCompat.getDrawable(map.context, com.example.floatingflavors.R.drawable.ic_marker_restaurant)
                        map.overlays.add(this)
                    }
                } else {
                    startMarker?.position = startPoint
                }
            }

            // B) Destination
            destination?.let {
                if (destMarker == null) {
                    destMarker = Marker(map).apply {
                        position = it
                        title = "Customer"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        icon = androidx.core.content.ContextCompat.getDrawable(map.context, com.example.floatingflavors.R.drawable.ic_marker_home)
                        map.overlays.add(this)
                    }
                } else {
                    destMarker?.position = it
                }
            }

             // C) LIVE DRIVER (Smart Rotation)
             if (livePoint != null) {
                 if (liveMarker == null) {
                    liveMarker = Marker(map).apply {
                        position = livePoint
                        title = "You"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = androidx.core.content.ContextCompat.getDrawable(map.context, com.example.floatingflavors.R.drawable.ic_marker_scooter)
                        rotation = if (bearing > 0) bearing else compassBearing
                        map.overlays.add(this)
                    }
                } else {
                    val marker = liveMarker!!
                    val dist = marker.position.distanceToAsDouble(livePoint)
                    
                    // 🔥 Smart Logic: If moving -> GPS Bearing. If stopped -> Compass.
                    val targetRotation = if (bearing != 0f) bearing else compassBearing
                    
                    if (dist > 0.5) { 
                         // Check distance threshold to update position
                         // Use smooth rotation always
                         SmoothMarkerAnimator.animate(marker, marker.position, livePoint, marker.rotation, targetRotation, map)
                    } else {
                         // Only rotate if stationary (turning in place)
                         if (kotlin.math.abs(marker.rotation - targetRotation) > 5) {
                             marker.rotation = targetRotation
                             map.invalidate()
                         }
                    }
                }
             }

             // 3. CAMERA LOGIC
            if (isNavigationStarted && livePoint != null) {
                // ✅ NAVIGATION MODE
                map.controller.animateTo(livePoint)
                if (map.zoomLevelDouble < 19.0) map.controller.setZoom(19.5)
                
                // If moving use GPS, else use Compass for Map Rotation too?
                // Usually map rotation follows travel direction (GPS). 
                map.mapOrientation = -bearing // Keep Map aligned to travel path
            } else {
                // ✅ MANUAL MODE
                if (map.mapOrientation != 0f) {
                    map.mapOrientation = 0f
                }
            }
            
            map.invalidate()
        }
    )
}

// Helper to split route (Naive implementation: finds closest point index)
private fun splitRouteAtPoint(route: List<GeoPoint>, userLoc: GeoPoint): Pair<List<GeoPoint>, List<GeoPoint>> {
    var closestIndex = 0
    var minDist = Double.MAX_VALUE
    
    // Find closest vertex (simplified projection)
    for (i in route.indices) {
        val dist = route[i].distanceToAsDouble(userLoc)
        if (dist < minDist) {
            minDist = dist
            closestIndex = i
        }
    }
    
    // Split
    // Traveled: includes start up to closest + userLoc
    val traveled = route.take(closestIndex + 1).toMutableList()
    traveled.add(userLoc)
    
    // Remaining: userLoc + from closest to end
    val remaining = mutableListOf<GeoPoint>()
    remaining.add(userLoc)
    remaining.addAll(route.drop(closestIndex + 1))
    
    return Pair(traveled, remaining)
}
