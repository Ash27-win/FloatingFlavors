package com.example.floatingflavors.app.feature.user.data.booking.dto

// EVEN USER CAN LOGOUT AGAIN LOGIN SHOW MENU SELECTED API HELPER FILE
data class BookingMenuItemsResponse(
    val success: Boolean,
    val data: List<BookingMenuItemSelectionDto>
)