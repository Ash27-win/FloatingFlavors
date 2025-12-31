package com.example.floatingflavors.app.feature.user.data.order

import com.example.floatingflavors.app.feature.user.data.order.dto.UserOrderDetailDto

class UserOrdersRepository(
    private val api: UserOrdersApi
) {
    suspend fun getOrderDetailsScreen(orderId: String): UserOrderDetailDto? {
        val res = api.getOrderDetail(orderId)
        return if (res.success) res.data else null
    }
}

