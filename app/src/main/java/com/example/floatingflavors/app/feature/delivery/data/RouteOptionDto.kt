package com.example.floatingflavors.app.feature.delivery.data

data class RouteOptionDto(
    val id: Int,
    val distanceKm: Double,
    val etaMin: Int,
    val polyline: List<Pair<Double, Double>>,
    val instructions: List<NavigationInstructionDto>
)
