package com.example.floatingflavors.app.feature.user.data.order.dto

data class UserOrderDetailDto(
    val id: String?,
    val customer_name: String?,
    val items: List<UserOrderDetailItemDto>?,
    val status: String?,
    val created_at: String?,
    val time_ago: String?,
    val amount: String?
)
