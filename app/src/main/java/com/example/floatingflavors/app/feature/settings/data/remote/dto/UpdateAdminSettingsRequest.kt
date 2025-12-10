package com.example.floatingflavors.app.feature.settings.data.remote.dto

data class UpdateAdminSettingsRequest(
    val admin_id: Int,
    val full_name: String,
    val email: String,
    val phone: String,
    val business_name: String,
    val address: String
)
