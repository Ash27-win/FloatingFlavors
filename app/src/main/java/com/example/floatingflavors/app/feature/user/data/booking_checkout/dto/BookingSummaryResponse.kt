package com.example.floatingflavors.app.feature.user.data.booking_checkout.dto

data class BookingSummaryResponse(
    val success: Boolean,
    val event: BookingEventDto,
    val items: List<BookingItemDto>,
    val total_amount: Double
)
