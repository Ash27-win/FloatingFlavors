package com.example.floatingflavors.app.feature.user.data.booking.dto

data class SaveBookingMenuRequestDto(
    val booking_id: Int,
    val items: List<BookingMenuItemDto>
)