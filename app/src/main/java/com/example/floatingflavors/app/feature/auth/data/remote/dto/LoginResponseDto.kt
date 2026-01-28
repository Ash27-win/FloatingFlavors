package com.example.floatingflavors.app.feature.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    val success: Boolean,
    val message: String,
    val data: LoginDataDto?
)

data class LoginDataDto(
    val user: UserDto,
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    @SerializedName("loyalty_points") val loyaltyPoints: Int = 0
)