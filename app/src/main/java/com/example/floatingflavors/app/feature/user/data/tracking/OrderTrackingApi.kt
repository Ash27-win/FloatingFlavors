package com.example.floatingflavors.app.feature.user.data.tracking

import com.example.floatingflavors.app.feature.user.data.tracking.dto.LiveLocationResponse
import com.example.floatingflavors.app.feature.user.data.tracking.dto.OrderTrackingResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OrderTrackingApi {

    @GET("order_tracking_details.php")
    suspend fun getOrderTracking(
        @Query("order_id") orderId: Int,
        @Query("type") type: String
    ): OrderTrackingResponse

    @GET("get_order_location.php")
    suspend fun getLiveLocation(
        @Query("order_id") orderId: Int
    ): LiveLocationResponse

    @FormUrlEncoded
    @POST("update_order_location.php")
    suspend fun updateOrderLocation(
        @Field("order_id") orderId: Int,
        @Field("lat") lat: Double,
        @Field("lng") lng: Double
    )

}
