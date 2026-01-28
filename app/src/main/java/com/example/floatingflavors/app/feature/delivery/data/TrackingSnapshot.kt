package com.example.floatingflavors.app.feature.delivery.data

data class TrackingSnapshot(
    val deliveryLocation: LiveLocationData?,
    val deliveryAddress: DeliveryAddressDto?
)
