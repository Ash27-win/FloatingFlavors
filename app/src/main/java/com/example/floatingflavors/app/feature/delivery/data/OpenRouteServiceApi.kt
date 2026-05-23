package com.example.floatingflavors.app.feature.delivery.data

import retrofit2.http.GET
import retrofit2.http.Query

interface OpenRouteServiceApi {
    @GET("https://api.openrouteservice.org/v2/directions/driving-car")
    suspend fun getDirections(
        @Query("api_key") apiKey: String,
        @Query("start") start: String, // "lon,lat" format
        @Query("end") end: String,     // "lon,lat" format
        @Query("alternatives") alternatives: Boolean = true,
        @Query("steps") steps: Boolean = true,
        @Query("annotations") annotations: String = "congestion"
    ): OrsDirectionsResponse
}

data class OrsDirectionsResponse(
    val features: List<OrsFeature>
)

data class OrsFeature(
    val geometry: OrsGeometry,
    val properties: OrsProperties
)

data class OrsGeometry(
    val coordinates: List<List<Double>> // [[lon, lat], [lon, lat]]
)

data class OrsProperties(
    val segments: List<OrsSegment>,
    val summary: OrsSummary
)

data class OrsSegment(
    val distance: Double,
    val duration: Double,
    val steps: List<OrsStep>
)

data class OrsStep(
    val distance: Double,
    val duration: Double,
    val instruction: String,
    val name: String,
    val way_points: List<Int>
)

data class OrsSummary(
    val distance: Double,
    val duration: Double
)
