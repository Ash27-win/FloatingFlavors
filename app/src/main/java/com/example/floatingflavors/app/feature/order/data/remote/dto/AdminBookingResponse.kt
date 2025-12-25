package com.example.floatingflavors.app.feature.order.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AdminBookingResponse(
    val success: Boolean,
    val has_booking: Boolean,
    val data: AdminBookingDto?
)
