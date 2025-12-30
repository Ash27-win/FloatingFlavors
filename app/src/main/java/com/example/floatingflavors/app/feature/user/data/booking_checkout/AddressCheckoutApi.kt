package com.example.floatingflavors.app.feature.user.data.booking_checkout

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import com.example.floatingflavors.app.feature.user.data.booking_checkout.dto.AddressCheckoutResponse

interface AddressCheckoutApi {

    @GET("user_addresses_get.php")
    suspend fun getAddresses(
        @Query("user_id") userId: Int
    ): AddressCheckoutResponse


    @FormUrlEncoded
    @POST("user_address_set_default.php")
    suspend fun setDefaultAddress(
        @Field("user_id") userId: Int,
        @Field("address_id") addressId: Int
    ): ApiStatusResponse
}

data class ApiStatusResponse(
    val status: Boolean,
    val message: String
)
