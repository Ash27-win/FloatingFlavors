package com.example.floatingflavors.app.feature.user.data.cart.dto

import com.google.gson.annotations.SerializedName

data class CartItemDto(
    @SerializedName("cart_item_id")
    val cartItemId: Int,

    @SerializedName("menu_item_id")
    val menuItemId: Int,   // ✅ ADD THIS

    val name: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    val quantity: Int,
    val price: Int,
    val subtotal: Int
)

