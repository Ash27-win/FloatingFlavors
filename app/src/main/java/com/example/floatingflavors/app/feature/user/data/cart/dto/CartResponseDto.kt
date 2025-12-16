package com.example.floatingflavors.app.feature.user.data.cart.dto

data class CartResponseDto(
    val success: Boolean,
    val items: List<CartItemDto>,
    val total: Int
)
