package com.example.floatingflavors.app.feature.delivery.data

data class RouteDto(
    val paths: List<RoutePathDto>
)

data class RoutePathDto(
    val distance: Double,          // meters
    val time: Long,                // ms
    val points: RoutePointsDto,
    val instructions: List<RouteInstructionDto>
)

data class RoutePointsDto(
    val coordinates: List<List<Double>> // [lng, lat]
)

data class RouteInstructionDto(
    val text: String,
    val distance: Double,           // meters
    val time: Long
)
