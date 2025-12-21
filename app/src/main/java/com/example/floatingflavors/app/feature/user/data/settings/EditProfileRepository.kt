package com.example.floatingflavors.app.feature.user.data.settings

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class EditProfileRepository(
    private val api: EditProfileApi
) {

    suspend fun getProfile(userId: Int) =
        api.getProfile(userId)

    suspend fun updateProfile(
        userId: Int,
        name: String,
        phone: String,
        altPhone: String?,
        pincode: String,
        city: String,
        house: String,
        area: String,
        landmark: String?,
        imagePart: MultipartBody.Part?
    ) = api.updateProfile(
        userId.toString().toRequestBody(),   // ✅ FIXED
        name.toRequestBody(),
        phone.toRequestBody(),
        altPhone?.toRequestBody(),
        pincode.toRequestBody(),
        city.toRequestBody(),
        house.toRequestBody(),
        area.toRequestBody(),
        landmark?.toRequestBody(),
        imagePart
    )
}

private fun String.toRequestBody(): RequestBody =
    this.toRequestBody("text/plain".toMediaType())
