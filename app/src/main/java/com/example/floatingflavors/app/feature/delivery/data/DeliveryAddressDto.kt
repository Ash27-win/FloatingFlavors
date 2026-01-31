package com.example.floatingflavors.app.feature.delivery.data

data class DeliveryAddressDto(
    val line1: String,
    val city: String,
    val pincode: String,
    val note: String?,
    val latitude: Double?,
    val longitude: Double?
)
