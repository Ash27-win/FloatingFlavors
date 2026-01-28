package com.example.floatingflavors.app.feature.delivery.data

class DeliveryTrackingRepository(
    private val api: DeliveryTrackingApi
) {

    // ✅ EXISTING – DO NOT REMOVE
    suspend fun getLiveLocation(orderId: Int): LiveLocationResponse =
        api.getLiveLocation(orderId)

    // ✅ ADD THIS (FIX for getOrderTracking unresolved ref)
    suspend fun getOrderTracking(orderId: Int): TrackingSnapshot {
        val live = api.getLiveLocation(orderId)
        val order = api.getTrackingDetails(orderId) // backend tracking api

        return TrackingSnapshot(
            deliveryLocation = live.location,
            deliveryAddress = order.deliveryAddress
        )
    }

    suspend fun markArrived(orderId: Int) =
        api.markArrived(orderId)

    suspend fun fetchRoutes(
        sLat: Double,
        sLng: Double,
        eLat: Double,
        eLng: Double
    ): List<RouteOptionDto> {

        val res = api.getRoutes(sLat, sLng, eLat, eLng)

        return res.paths.mapIndexed { index, path ->
            RouteOptionDto(
                id = index,
                distanceKm = path.distance / 1000,
                etaMin = (path.time / 60000).toInt(),
                polyline = path.points.coordinates.map {
                    it[1] to it[0] // lat, lng
                },
                instructions = path.instructions.map {
                    NavigationInstructionDto(
                        text = it.text,
                        distanceMeters = it.distance
                    )
                }
            )
        }
    }
}
