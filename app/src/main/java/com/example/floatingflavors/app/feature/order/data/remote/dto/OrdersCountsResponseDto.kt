package com.example.floatingflavors.app.feature.order.data.remote.dto

data class OrdersCountsResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val data: OrdersCounts? = null
)

data class OrdersCounts(val total: Int = 0, val pending: Int = 0, val active: Int = 0, val completed: Int = 0)
