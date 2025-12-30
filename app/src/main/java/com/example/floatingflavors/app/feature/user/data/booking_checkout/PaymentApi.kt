package com.example.floatingflavors.app.feature.user.data.booking_checkout

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface PaymentApi {

    @FormUrlEncoded
    @POST("mark_booking_paid.php")
    suspend fun markPaid(
        @Field("booking_id") bookingId: Int,
        @Field("txn_id") txnId: String,
        @Field("method") method: String
    )
}
