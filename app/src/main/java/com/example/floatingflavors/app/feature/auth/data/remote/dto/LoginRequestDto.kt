package com.example.floatingflavors.app.feature.auth.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val password: String,
    val role: String
)
