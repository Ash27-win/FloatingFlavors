package com.example.floatingflavors.app.feature.user.data.booking.dto

data class BookingRequest(
    val userId: Int,
    val bookingType: String,

    val eventType: String? = null,
    val eventName: String? = null,
    val peopleCount: Int? = null,
    val eventDate: String? = null,
    val eventTime: String? = null,

    val companyName: String? = null,
    val contactPerson: String? = null,
    val employeeCount: Int? = null,
    val contractDuration: String? = null,
    val serviceFrequency: String? = null,
    val notes: String? = null
)
