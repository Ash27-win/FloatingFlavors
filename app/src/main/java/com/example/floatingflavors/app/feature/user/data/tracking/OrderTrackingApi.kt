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

    @GET("get_live_location.php")
    suspend fun getLiveLocation(
        @Query("order_id") orderId: Int
    ): LiveLocationResponse

    // Used by delivery/driver logic mostly, but if user app has driver mode
    @FormUrlEncoded
    @POST("update_live_location.php") 
    suspend fun updateLiveLocation(
        @Field("order_id") orderId: Int,
        @Field("latitude") lat: Double,
        @Field("longitude") lng: Double,
        @Field("delivery_partner_id") deliveryPartnerId: Int
    ): SimpleResponseDto

}

data class SimpleResponseDto(val success: Boolean, val message: String?)
