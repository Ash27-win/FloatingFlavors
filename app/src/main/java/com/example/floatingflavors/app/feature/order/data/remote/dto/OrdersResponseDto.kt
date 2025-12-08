// OrdersResponseDto.kt
package com.example.floatingflavors.app.feature.orders.data.remote.dto

data class OrdersResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<OrderDto>? = emptyList()
)
