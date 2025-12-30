package com.example.floatingflavors.app.feature.user.data.booking_checkout.dto

data class BookingEventDto(
    val booking_id: String,
    val booking_type: String,
    val event_type: String,
    val event_name: String?,
    val people_count: String,
    val event_date: String,
    val event_time: String,
    val status: String
)
