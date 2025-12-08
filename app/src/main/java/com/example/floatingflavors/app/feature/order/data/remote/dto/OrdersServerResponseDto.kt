package com.example.floatingflavors.app.feature.order.data.remote.dto

import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrderDto


data class OrdersServerResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<OrderDto>? = emptyList(),
    val meta: OrdersMetaDto? = null
)

data class OrdersMetaDto(val total: Int = 0, val limit: Int = 0, val offset: Int = 0)
