package com.example.floatingflavors.app.feature.user.presentation.menu.cart

import com.example.floatingflavors.app.feature.user.data.cart.dto.CartItemDto

sealed class CartUiState {
    object Loading : CartUiState()
    data class Success(
        val items: List<CartItemDto>,
        val total: Int
    ) : CartUiState()

    data class Error(val message: String) : CartUiState()
}


