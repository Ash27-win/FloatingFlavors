package com.example.floatingflavors.app.feature.order.data.remote.dto

import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrderDto

// OrderDetailResponseDto.kt
data class OrderDetailResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val data: OrderDto? = null
)
