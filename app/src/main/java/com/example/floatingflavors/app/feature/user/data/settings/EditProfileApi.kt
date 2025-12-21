package com.example.floatingflavors.app.feature.user.data.settings

import com.example.floatingflavors.app.feature.user.data.settings.dto.EditProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface EditProfileApi {

    @GET("user_profile_get.php")
    suspend fun getProfile(
        @Query("user_id") userId: Int
    ): EditProfileResponse

    @Multipart
    @POST("settings_update_profile.php")
    suspend fun updateProfile(
        @Part("user_id") userId: RequestBody,
        @Part("name") name: RequestBody,
        @Part("phone") phone: RequestBody,
        @Part("alt_phone") altPhone: RequestBody?,
        @Part("pincode") pincode: RequestBody,
        @Part("city") city: RequestBody,
        @Part("house") house: RequestBody,
        @Part("area") area: RequestBody,
        @Part("landmark") landmark: RequestBody?,
        @Part profile_image: MultipartBody.Part?
    ): EditProfileResponse
}

