package com.example.floatingflavors.app.feature.user.presentation.tracking

import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun OsmMapComposable(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val markerRef = remember { mutableStateOf<Marker?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Configuration.getInstance()
                .load(context, context.getSharedPreferences("osm", 0))

            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(18.0)
                controller.setCenter(GeoPoint(latitude, longitude))
            }
        },
        update = { map ->
            val geoPoint = GeoPoint(latitude, longitude)

            if (markerRef.value == null) {
                markerRef.value = Marker(map).apply {
                    position = geoPoint
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                map.overlays.add(markerRef.value)
            } else {
                markerRef.value!!.position = geoPoint
            }

            map.controller.animateTo(geoPoint)
            map.invalidate()
        }
    )
}

