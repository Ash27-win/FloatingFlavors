// OrderDto.kt
package com.example.floatingflavors.app.feature.orders.data.remote.dto

data class OrderDto(
    val id: String?,
    val customer_name: String?,
    val items: List<OrdersItemDto>?,
    val status: String?,
    val created_at: String?,   // ISO 8601 string from backend
    val time_ago: String?,
    val distance: String?,
    val amount: String?,
    val isBooking: Boolean = false,
    val bookingId: String? = null
)
