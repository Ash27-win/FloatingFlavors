package com.example.floatingflavors.app.feature.delivery.presentation.tracking.data

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface DeliveryLocationApi {

    @FormUrlEncoded
    @POST("update_order_location.php")
    suspend fun updateLocation(
        @Field("order_id") orderId: Int,
        @Field("lat") lat: Double,
        @Field("lng") lng: Double,
        @Field("delivery_partner_id") deliveryPartnerId: Int
    )
}
