package com.example.floatingflavors.app.feature.user.data.settings.dto

data class EditAddressRequest(
    val address_id: Int,
    val user_id: Int,
    val label: String,
    val house: String,
    val area: String,
    val pincode: String,
    val city: String,
    val landmark: String?,
    val is_default: Int
)

