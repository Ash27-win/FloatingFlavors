package com.example.floatingflavors.app.feature.delivery.presentation

import kotlin.math.*

object RouteUtils {

    fun distanceMeters(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2).pow(2.0) +
                    cos(Math.toRadians(lat1)) *
                    cos(Math.toRadians(lat2)) *
                    sin(dLon / 2).pow(2.0)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    fun nearestPointIndex(
        lat: Double,
        lng: Double,
        poly: List<Pair<Double, Double>>
    ): Int {
        var minDist = Double.MAX_VALUE
        var index = 0

        poly.forEachIndexed { i, p ->
            val d = distanceMeters(lat, lng, p.first, p.second)
            if (d < minDist) {
                minDist = d
                index = i
            }
        }
        return index
    }
}
