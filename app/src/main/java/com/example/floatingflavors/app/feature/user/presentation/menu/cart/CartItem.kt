package com.example.floatingflavors.app.feature.user.presentation.menu.cart


data class CartItem(
    val cartItemId: Int,
    val menuItemId: Int,
    val name: String,
    val quantity: Int,
    val price: Int
)

