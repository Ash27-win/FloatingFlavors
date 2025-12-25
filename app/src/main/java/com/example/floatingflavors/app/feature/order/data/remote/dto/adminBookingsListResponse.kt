package com.example.floatingflavors.app.feature.order.data.remote.dto

data class AdminBookingsListResponse(
    val success: Boolean,
    val data: List<AdminBookingDto>
)
