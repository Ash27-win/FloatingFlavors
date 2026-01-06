package com.example.floatingflavors.app.feature.order.data.remote.dto

import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrderDto
import com.google.gson.annotations.SerializedName

// OrderDetailResponseDto.kt
data class OrderDetailResponseDto(
    @SerializedName(value = "status", alternate = ["success"])
    val status: Boolean,
    val message: String,
    val data: OrderDto?
)
