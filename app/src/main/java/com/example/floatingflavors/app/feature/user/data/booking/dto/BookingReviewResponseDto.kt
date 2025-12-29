package com.example.floatingflavors.app.feature.user.data.booking.dto

data class BookingReviewResponseDto(
    val success: Boolean,
    val event: BookingEventInfoDto,
    val items: List<ReviewDishDto>,
    val total_amount: Double
)
