package com.example.floatingflavors.app.feature.user.data.tracking

// OSRM API HELPER

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.GeoPoint

object OsrmRouteService {

    private val client = OkHttpClient()

    suspend fun fetchRoute(
        start: GeoPoint,
        end: GeoPoint
    ): List<GeoPoint> = withContext(Dispatchers.IO) {

        val url =
            "https://router.project-osrm.org/route/v1/driving/" +
                    "${start.longitude},${start.latitude};" +
                    "${end.longitude},${end.latitude}" +
                    "?overview=full&geometries=geojson"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        val body = response.body?.string() ?: return@withContext emptyList()

        val json = JSONObject(body)
        val coords = json
            .getJSONArray("routes")
            .getJSONObject(0)
            .getJSONObject("geometry")
            .getJSONArray("coordinates")

        val points = mutableListOf<GeoPoint>()
        for (i in 0 until coords.length()) {
            val p = coords.getJSONArray(i)
            points.add(GeoPoint(p.getDouble(1), p.getDouble(0)))
        }

        points
    }
}
