package com.example.floatingflavors.app.feature.user.data.tracking

// OSRM API HELPER

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.osmdroid.util.GeoPoint

data class TurnInstruction(
    val text: String,
    val distanceMeters: Int
)

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

    // 🔥 NEW — REAL ETA FROM OSRM
    suspend fun fetchRouteWithEta(
        start: GeoPoint,
        end: GeoPoint
    ): Pair<List<GeoPoint>, Int> = withContext(Dispatchers.IO) {

        val url =
            "https://router.project-osrm.org/route/v1/driving/" +
                    "${start.longitude},${start.latitude};" +
                    "${end.longitude},${end.latitude}" +
                    "?overview=full&geometries=geojson"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext emptyList<GeoPoint>() to 0

        val json = JSONObject(body)
        val route = json.getJSONArray("routes").getJSONObject(0)

        val durationMin = (route.getDouble("duration") / 60).toInt()

        val coords = route
            .getJSONObject("geometry")
            .getJSONArray("coordinates")

        val points = mutableListOf<GeoPoint>()
        for (i in 0 until coords.length()) {
            val p = coords.getJSONArray(i)
            points.add(GeoPoint(p.getDouble(1), p.getDouble(0)))
        }

        points to durationMin
    }

    data class RouteResult(
        val points: List<GeoPoint>,
        val durationMin: Int,
        val distanceMeters: Int,
        val description: String, // e.g. "Main Road"
        val instructions: List<TurnInstruction> = emptyList() // 🔥 NEW
    )

    suspend fun fetchMultipleRoutes(
        start: GeoPoint,
        end: GeoPoint
    ): List<RouteResult> = withContext(Dispatchers.IO) {

        val url =
            "https://router.project-osrm.org/route/v1/driving/" +
                    "${start.longitude},${start.latitude};" +
                    "${end.longitude},${end.latitude}" +
                    "?overview=full&geometries=geojson&steps=true&alternatives=true"

        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()
        val body = response.body?.string() ?: return@withContext emptyList()

        val json = JSONObject(body)
        val routesJson = json.optJSONArray("routes") ?: return@withContext emptyList()

        val results = mutableListOf<RouteResult>()

        for (k in 0 until routesJson.length()) {
            val route = routesJson.getJSONObject(k)
            val durationMin = (route.getDouble("duration") / 60).toInt()
            val distMeters = route.getDouble("distance").toInt()

            // Extract Name (Description) & Full Instructions
            var description = "Route ${k + 1}"
            val legs = route.optJSONArray("legs")
            val instructionsList = mutableListOf<TurnInstruction>()

            if (legs != null && legs.length() > 0) {
                 val steps = legs.getJSONObject(0).optJSONArray("steps")
                 if (steps != null && steps.length() > 0) {
                     val firstStep = steps.getJSONObject(0)
                     description = firstStep.optString("name", "Unknown Road")
                     if (description.isEmpty()) description = "Route ${k + 1}"
                     
                     // 🔹 Parse all steps for navigation
                     for (s in 0 until steps.length()) {
                         val stepObj = steps.getJSONObject(s)
                         val maneuver = stepObj.optJSONObject("maneuver")
                         val type = maneuver?.optString("type") ?: ""
                         val modifier = maneuver?.optString("modifier") ?: ""
                         val name = stepObj.optString("name")
                         
                         // Clean text generation (basic)
                         val text = if (name.isNotEmpty()) "$type $modifier onto $name" else "$type $modifier"
                         
                         instructionsList.add(TurnInstruction(text, stepObj.optInt("distance")))
                     }
                 }
            }

            // Points
            val coords = route
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val points = mutableListOf<GeoPoint>()
            for (i in 0 until coords.length()) {
                val p = coords.getJSONArray(i)
                points.add(GeoPoint(p.getDouble(1), p.getDouble(0)))
            }

            results.add(RouteResult(points, durationMin, distMeters, description, instructionsList))
        }

        results
    }

    suspend fun fetchRouteWithTurns(
        start: GeoPoint,
        end: GeoPoint
    ): Triple<List<GeoPoint>, Int, List<TurnInstruction>> =
        withContext(Dispatchers.IO) {

            val url =
                "https://router.project-osrm.org/route/v1/driving/" +
                        "${start.longitude},${start.latitude};" +
                        "${end.longitude},${end.latitude}" +
                        "?overview=full&geometries=geojson&steps=true"

            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string()
                ?: return@withContext Triple(
                    emptyList(),
                    0,
                    emptyList()
                )

            val json = JSONObject(body)
            val route = json.getJSONArray("routes").getJSONObject(0)

            val etaMin = (route.getDouble("duration") / 60).toInt()

            // 🔹 Geometry
            val coords = route
                .getJSONObject("geometry")
                .getJSONArray("coordinates")

            val points = mutableListOf<GeoPoint>()
            for (i in 0 until coords.length()) {
                val p = coords.getJSONArray(i)
                points.add(GeoPoint(p.getDouble(1), p.getDouble(0)))
            }

            // 🔹 Turn instructions
            val steps = route
                .getJSONArray("legs")
                .getJSONObject(0)
                .getJSONArray("steps")

            val turns = mutableListOf<TurnInstruction>()
            for (i in 0 until steps.length()) {
                val s = steps.getJSONObject(i)
                turns.add(
                    TurnInstruction(
                        text = s.getString("name"),
                        distanceMeters = s.getInt("distance")
                    )
                )
            }

            Triple(points, etaMin, turns)
        }
}
