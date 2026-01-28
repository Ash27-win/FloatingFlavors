package com.example.floatingflavors.app.feature.user.data.settings.dto

data class AddressDto(
    val id: Int,
    val label: String,
    val pincode: String,
    val city: String,
    val house: String,
    val area: String,
    val landmark: String?,
    val is_default: Int,
    val latitude: Double? = 0.0,
    val longitude: Double? = 0.0
)
