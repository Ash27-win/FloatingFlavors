package com.example.floatingflavors.app.feature.user.data.settings.dto

data class UpdateProfileRequest(
    val user_id: Int,
    val name: String,
    val email: String
)
