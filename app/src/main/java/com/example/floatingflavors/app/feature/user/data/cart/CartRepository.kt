package com.example.floatingflavors.app.feature.user.data.cart

import com.example.floatingflavors.app.core.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartRepository {

    private val api = NetworkClient.cartApi

    suspend fun fetchCart(userId: Int) = withContext(Dispatchers.IO) {
        api.getCart(userId)
    }

    suspend fun addItem(userId: Int, menuId: Int, price: Int) =
        api.addToCart(userId, menuId, price)

    suspend fun increase(cartItemId: Int) =
        api.updateQuantity(cartItemId, "increase")

    suspend fun decrease(cartItemId: Int) =
        api.updateQuantity(cartItemId, "decrease")

    suspend fun remove(cartItemId: Int) =
        api.removeItem(cartItemId)
}
