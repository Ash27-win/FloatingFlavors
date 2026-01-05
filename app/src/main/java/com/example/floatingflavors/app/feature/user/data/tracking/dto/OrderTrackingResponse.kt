package com.example.floatingflavors.app.feature.user.data.tracking.dto

data class OrderTrackingResponse(
    val success: Boolean,
    val orderNumber: String, // Changed from orderId
    val orderType: String,
    val currentStatus: String,
    val eventInfo: EventInfo,
    val statusTimeline: List<StatusItem>,
    val deliveryPerson: DeliveryPerson?, // Added
    val deliveryLocation: LiveLocation?, // Added
    val deliveryAddress: Address
)