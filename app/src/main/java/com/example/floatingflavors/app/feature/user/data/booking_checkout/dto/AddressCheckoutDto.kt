package com.example.floatingflavors.app.feature.user.data.booking_checkout.dto

data class AddressCheckoutDto(
    val id: Int,
    val user_id: Int,
    val label: String,
    val custom_label: String?,
    val house: String,
    val area: String,
    val city: String,
    val pincode: String,
    val landmark: String?,
    val is_default: Int
)
