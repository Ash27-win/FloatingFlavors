package com.example.floatingflavors.app.feature.delivery.data

import retrofit2.http.*

interface DeliveryTrackingApi {

    @GET("get_route.php")
    suspend fun getRoutes(
        @Query("start_lat") startLat: Double,
        @Query("start_lng") startLng: Double,
        @Query("end_lat") endLat: Double,
        @Query("end_lng") endLng: Double
    ): RouteDto

    @GET("get_order_location.php")
    suspend fun getLiveLocation(
        @Query("order_id") orderId: Int
    ): LiveLocationResponse

    /* ================= 🔴 FIX START ================= */
    @GET("delivery_tracking.php")
    suspend fun getTrackingDetails(
        @Query("order_id") orderId: Int,
        @Query("type") type: String = "INDIVIDUAL"
    ): TrackingResponseDto
    /* ================= 🔴 FIX END ================= */

    @FormUrlEncoded
    @POST("mark_arrived.php")
    suspend fun markArrived(
        @Field("order_id") orderId: Int
    ): SimpleResponse
}

data class SimpleResponse(
    val success: Boolean,
    val message: String
)
