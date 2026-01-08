package com.example.floatingflavors.app.feature.user.presentation.tracking

import android.os.Build
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.floatingflavors.app.feature.user.data.tracking.OsrmRouteService
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SmoothLiveTrackingMap(
    livePoint: GeoPoint?,
    destination: GeoPoint?,
    modifier: Modifier = Modifier
) {
    var marker by remember { mutableStateOf<Marker?>(null) }
    var polyline by remember { mutableStateOf<Polyline?>(null) }

    val routePoints = remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    LaunchedEffect(livePoint, destination) {
        if (livePoint != null && destination != null) {
            routePoints.value =
                OsrmRouteService.fetchRoute(livePoint, destination)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Configuration.getInstance()
                .load(context, context.getSharedPreferences("osm", 0))

            MapView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(18.0)
            }
        },
        update = { map ->
            if (destination == null) return@AndroidView

            // 🏁 Destination Marker (ALWAYS)
            if (map.overlays.none { it is Marker && it.title == "Destination" }) {
                map.overlays.add(
                    Marker(map).apply {
                        position = destination
                        title = "Destination"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                )
            }

// 🚚 Live Marker (ONLY IF AVAILABLE)
            if (livePoint != null) {
                if (marker == null) {
                    marker = Marker(map).apply {
                        position = livePoint
                        title = "Delivery Partner"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    map.overlays.add(marker)
                } else {
                    animateMarkerSmooth(marker!!, marker!!.position, livePoint)
                }
            }


            // 🏁 Destination Marker
            if (map.overlays.none { it is Marker && it.title == "Destination" }) {
                map.overlays.add(
                    Marker(map).apply {
                        position = destination
                        title = "Destination"
                    }
                )
            }

            // 🟢 Polyline
            if (polyline == null) {
                polyline = Polyline().apply {
                    outlinePaint.strokeWidth = 6f
                }
                map.overlays.add(polyline)
            }

            if (routePoints.value.isNotEmpty()) {
                polyline!!.setPoints(routePoints.value)
            }

//            polyline!!.setPoints(listOf(livePoint, destination))

            map.controller.animateTo(livePoint)
            map.invalidate()
        }
    )
}

fun animateMarkerSmooth(
    marker: Marker,
    from: GeoPoint,
    to: GeoPoint
) {
    val steps = 30
    for (i in 1..steps) {
        val lat =
            from.latitude + (to.latitude - from.latitude) * i / steps
        val lng =
            from.longitude + (to.longitude - from.longitude) * i / steps
        marker.position = GeoPoint(lat, lng)
    }
}
