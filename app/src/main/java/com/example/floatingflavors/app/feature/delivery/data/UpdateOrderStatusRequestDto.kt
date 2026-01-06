package com.example.floatingflavors.app.feature.delivery.data

data class UpdateOrderStatusRequestDto(
    val order_id: Int,
    val status: String,
    val delivery_partner_id: Int? = null
)
