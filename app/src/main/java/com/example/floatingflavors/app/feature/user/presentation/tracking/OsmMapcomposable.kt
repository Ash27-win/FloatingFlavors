package com.example.floatingflavors.app.feature.user.presentation.tracking

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

data class RouteSegment(
    val points: List<GeoPoint>,
    val color: Int // Android Color (Green/Amber/Red)
)

data class NavigationRoute(
    val id: String,
    val segments: List<RouteSegment>,
    val totalDistanceKm: Double,
    val totalEtaMinutes: Int,
    val isFastest: Boolean = false
)

@Composable
fun OsmMapComposable(
    latitude: Double,
    longitude: Double,
    bearing: Float = 0f,
    speedKmh: Double = 0.0,
    modifier: Modifier = Modifier,
    routes: List<NavigationRoute> = emptyList(),
    activeRouteId: String? = null,
    isNavigating: Boolean = false,
    onRouteSelected: ((String) -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapRef = remember { mutableStateOf<MapView?>(null) }
    val renderController = remember { mutableStateOf<MapRenderController?>(null) }
    
    // Lifecycle Management for Memory Leak Prevention
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapRef.value?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapRef.value?.onPause()
                Lifecycle.Event.ON_DESTROY -> {
                    renderController.value?.dispose()
                    mapRef.value?.onDetach()
                    mapRef.value = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            renderController.value?.dispose()
            mapRef.value?.onDetach()
            mapRef.value = null
        }
    }

    // Imperative UI Updates bypassing Compose Recomposition
    LaunchedEffect(latitude, longitude, bearing, speedKmh, isNavigating) {
        renderController.value?.updateMarkerPosition(GeoPoint(latitude, longitude), bearing, speedKmh, isNavigating)
    }
    
    LaunchedEffect(routes, activeRouteId, isNavigating) {
        renderController.value?.updateRoutes(routes, activeRouteId, onRouteSelected, isNavigating)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            val config = Configuration.getInstance()
            config.load(context, context.getSharedPreferences("osm", 0))
            config.userAgentValue = "FloatingFlavorsApp/1.0"
            val osmCache = java.io.File(context.cacheDir, "osmdroid")
            osmCache.mkdirs() // Ensure directory exists
            config.osmdroidBasePath = osmCache
            config.osmdroidTileCache = osmCache

            val mapView = MapView(context)

            mapView.apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(18.0)
                controller.setCenter(GeoPoint(latitude, longitude))
                mapRef.value = this
                renderController.value = MapRenderController(this)
                onResume() // CRITICAL: Explicitly resume map to start tile downloads!
            }
        },
        update = {
            // Left intentionally blank. 
            // We use LaunchedEffect for imperative updates via MapRenderController 
            // to avoid extreme lag during continuous GPS polling.
        }
    )
}
