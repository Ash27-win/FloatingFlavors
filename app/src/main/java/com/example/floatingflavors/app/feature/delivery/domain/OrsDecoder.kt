package com.example.floatingflavors.app.feature.delivery.domain

import android.graphics.Color
import com.example.floatingflavors.app.feature.delivery.data.OrsFeature
import com.example.floatingflavors.app.feature.user.presentation.tracking.NavigationRoute
import com.example.floatingflavors.app.feature.user.presentation.tracking.RouteSegment
import org.osmdroid.util.GeoPoint
import java.util.UUID

object OrsDecoder {

    fun decodeRoutes(features: List<OrsFeature>): List<NavigationRoute> {
        val navigationRoutes = mutableListOf<NavigationRoute>()

        // Find the fastest route by duration
        val fastestIndex = features.indices.minByOrNull { features[it].properties.summary.duration } ?: 0

        features.forEachIndexed { index, feature ->
            val geometryCoords = feature.geometry.coordinates // [[lon, lat], [lon, lat]]
            val points = geometryCoords.map { GeoPoint(it[1], it[0]) } // Map to GeoPoint(lat, lon)
            
            // If the ORS API has congestion data, we would parse it here.
            // For this implementation, we simulate traffic colors based on segment indices for demonstration
            // since actual ORS congestion mapping requires parsing the `extras` object from the JSON.
            
            // To prevent crashing and gracefully handle the UI, we split the route into synthetic segments
            val segments = mutableListOf<RouteSegment>()
            val chunkedPoints = points.chunked(points.size / 3 + 1)
            
            val colors = listOf(
                Color.parseColor("#34A853"), // Green (Smooth)
                Color.parseColor("#FBBC05"), // Amber (Medium)
                Color.parseColor("#EA4335")  // Red (Heavy)
            )

            chunkedPoints.forEachIndexed { chunkIndex, chunk ->
                if (chunk.isNotEmpty()) {
                    // In a production ORS V2 environment with `annotations=congestion`, 
                    // we'd map the exact line ranges. Here we alternate for demo accuracy.
                    segments.add(RouteSegment(
                        points = chunk,
                        color = colors[chunkIndex % colors.size]
                    ))
                }
            }

            navigationRoutes.add(
                NavigationRoute(
                    id = "route_${UUID.randomUUID()}",
                    segments = segments,
                    totalDistanceKm = feature.properties.summary.distance / 1000.0,
                    totalEtaMinutes = (feature.properties.summary.duration / 60.0).toInt(),
                    isFastest = (index == fastestIndex)
                )
            )
        }

        return navigationRoutes
    }
}
