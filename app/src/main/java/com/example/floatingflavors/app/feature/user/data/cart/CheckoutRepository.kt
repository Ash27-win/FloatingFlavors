package com.example.floatingflavors.app.feature.user.data.cart

import com.example.floatingflavors.app.core.network.NetworkClient

class CheckoutRepository {
    private val api = NetworkClient.checkoutApi

    suspend fun getSummary(userId: Int) = api.getSummary(userId)
    suspend fun placeOrder(userId: Int, payment: String, addressId: Int) =
        api.placeOrder(userId, payment, addressId)
}
