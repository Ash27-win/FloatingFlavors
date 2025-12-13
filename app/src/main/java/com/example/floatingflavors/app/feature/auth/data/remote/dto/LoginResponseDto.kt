package com.example.floatingflavors.app.feature.auth.data.remote.dto

data class LoginResponseDto(
    val success: Boolean,
    val message: String,
    val data: UserDto?
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    val role: String
)