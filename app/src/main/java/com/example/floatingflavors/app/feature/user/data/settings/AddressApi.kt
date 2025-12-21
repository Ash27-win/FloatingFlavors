package com.example.floatingflavors.app.feature.user.data.settings

import com.example.floatingflavors.app.feature.admin.data.remote.dto.ApiResponse
import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AddressApi {

    @GET("user_addresses_get.php")
    suspend fun getAddresses(
        @Query("user_id") userId: Int
    ): ApiResponse<List<AddressDto>>

    @FormUrlEncoded
    @POST("user_address_add.php")
    suspend fun addAddress(
        @Field("user_id") userId: Int,
        @Field("label") label: String,
        @Field("house") house: String,
        @Field("area") area: String,
        @Field("pincode") pincode: String,
        @Field("city") city: String,
        @Field("landmark") landmark: String?
    ): ApiResponse<Unit>

    @FormUrlEncoded
    @POST("user_address_delete.php")
    suspend fun deleteAddress(
        @Field("address_id") id: Int
    ): ApiResponse<Unit>
}
