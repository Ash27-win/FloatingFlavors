package com.example.floatingflavors.app.feature.orders.data

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.menu.data.remote.dto.SimpleResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrderDetailResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersCountsResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersServerResponseDto
import com.example.floatingflavors.app.feature.orders.data.remote.OrdersApi
import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrdersResponseDto

class OrdersRepository {
    private val api: OrdersApi = NetworkClient.retrofit.create(OrdersApi::class.java)
    suspend fun getOrders(): OrdersResponseDto = api.getOrders()

    // server-side search & paging
    suspend fun getOrdersServer(query: String? = null, status: String? = null, limit: Int = 20, offset: Int = 0): OrdersServerResponseDto {
        return api.getOrdersServer(query, status, limit, offset)
    }

    suspend fun getOrdersCounts(): OrdersCountsResponseDto {
        return api.getOrdersCounts()
    }

    suspend fun getOrderDetail(id: Int): OrderDetailResponseDto {
        return NetworkClient.ordersApi.getOrderDetail(id)
    }

    suspend fun updateOrderStatus(orderId: Int, newStatus: String): SimpleResponseDto {
        return api.updateOrderStatus(orderId, newStatus)
    }
}
