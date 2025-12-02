package com.example.floatingflavors.app.feature.auth.data.remote.dto

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val role: String
)
