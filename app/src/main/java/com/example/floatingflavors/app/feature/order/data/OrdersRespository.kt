// REPLACE OrdersRepository.kt with this:
package com.example.floatingflavors.app.feature.orders.data

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.menu.data.remote.dto.SimpleResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrderDetailResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrderStatusUpdateResponse
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersCountsResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersServerResponseDto
import com.example.floatingflavors.app.feature.orders.data.remote.OrdersApi
import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrdersResponseDto
import com.example.floatingflavors.app.feature.settings.data.remote.dto.ApiResponse
import okhttp3.FormBody

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
        return api.getOrderDetail(id)
    }

    // ✅ SIMPLE & WORKING VERSION
    suspend fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        deliveryPartnerId: Int? = null
    ): ApiResponse<OrderStatusUpdateResponse> {
        return try {
            // Create form data
            val formBody = FormBody.Builder()
                .add("order_id", orderId)
                .add("status", newStatus)
                .apply {
                    deliveryPartnerId?.let {
                        add("delivery_partner_id", it.toString())
                    }
                }
                .build()

            // Call API with RequestBody
            api.updateOrderStatus(formBody)
        } catch (e: Exception) {
            ApiResponse(
                status = false,
                message = e.message ?: "Network error",
                data = null
            )
        }
    }

    // ✅ FIX THIS: Function to reject order with reason
    suspend fun rejectOrder(orderId: String, reason: String): ApiResponse<OrderStatusUpdateResponse> {
        return try {
            // Create form data with reject_reason
            val formBody = FormBody.Builder()
                .add("order_id", orderId)
                .add("status", "REJECTED")
                .add("reject_reason", reason)
                .build()

            // Call API with RequestBody
            api.updateOrderStatus(formBody)
        } catch (e: Exception) {
            ApiResponse(
                status = false,
                message = e.message ?: "Network error",
                data = null
            )
        }
    }

    // ✅ UPDATE THIS: Add reject_reason parameter
    suspend fun updateOrderStatusWithReason(
        orderId: String,
        newStatus: String,
        deliveryPartnerId: Int? = null,
        rejectReason: String? = null
    ): ApiResponse<OrderStatusUpdateResponse> {
        return try {
            // Create form data
            val formBody = FormBody.Builder()
                .add("order_id", orderId)
                .add("status", newStatus)
                .apply {
                    deliveryPartnerId?.let {
                        add("delivery_partner_id", it.toString())
                    }
                    rejectReason?.let {
                        add("reject_reason", it)
                    }
                }
                .build()

            // Call API with RequestBody
            api.updateOrderStatus(formBody)
        } catch (e: Exception) {
            ApiResponse(
                status = false,
                message = e.message ?: "Network error",
                data = null
            )
        }
    }

    // 🔥 Bookings
    suspend fun getEventBookings(): List<AdminBookingDto> {
        val response = api.getEventBookings()
        return if (response.success) { // Change from response.status to response.success
            response.data ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun updateBookingStatus(bookingId: String, status: String): SimpleResponseDto {
        return api.updateBookingStatus(bookingId, status)
    }
}







//// REPLACE OrdersRepository.kt with this:
//package com.example.floatingflavors.app.feature.orders.data
//
//import com.example.floatingflavors.app.core.network.NetworkClient
//import com.example.floatingflavors.app.feature.menu.data.remote.dto.SimpleResponseDto
//import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingDto
//import com.example.floatingflavors.app.feature.order.data.remote.dto.OrderDetailResponseDto
//import com.example.floatingflavors.app.feature.order.data.remote.dto.OrderStatusUpdateResponse
//import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersCountsResponseDto
//import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersServerResponseDto
//import com.example.floatingflavors.app.feature.orders.data.remote.OrdersApi
//import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrdersResponseDto
//import com.example.floatingflavors.app.feature.settings.data.remote.dto.ApiResponse
//import okhttp3.FormBody
//
//class OrdersRepository {
//    private val api: OrdersApi = NetworkClient.retrofit.create(OrdersApi::class.java)
//
//    suspend fun getOrders(): OrdersResponseDto = api.getOrders()
//
//    // server-side search & paging
//    suspend fun getOrdersServer(query: String? = null, status: String? = null, limit: Int = 20, offset: Int = 0): OrdersServerResponseDto {
//        return api.getOrdersServer(query, status, limit, offset)
//    }
//
//    suspend fun getOrdersCounts(): OrdersCountsResponseDto {
//        return api.getOrdersCounts()
//    }
//
//    suspend fun getOrderDetail(id: Int): OrderDetailResponseDto {
//        return api.getOrderDetail(id)
//    }
//
//    // ✅ SIMPLE & WORKING VERSION
//    suspend fun updateOrderStatus(
//        orderId: String,
//        newStatus: String,
//        deliveryPartnerId: Int? = null
//    ): ApiResponse<OrderStatusUpdateResponse> {
//        return try {
//            // Create form data
//            val formBody = FormBody.Builder()
//                .add("order_id", orderId)
//                .add("status", newStatus)
//                .apply {
//                    deliveryPartnerId?.let {
//                        add("delivery_partner_id", it.toString())
//                    }
//                }
//                .build()
//
//            // Call API with RequestBody
//            api.updateOrderStatus(formBody)
//        } catch (e: Exception) {
//            ApiResponse(
//                status = false,
//                message = e.message ?: "Network error",
//                data = null
//            )
//        }
//    }
//
//    // 🔥 Bookings
//    suspend fun getEventBookings(): List<AdminBookingDto> {
//        val response = api.getEventBookings()
//        return if (response.success) { // Change from response.status to response.success
//            response.data ?: emptyList()
//        } else {
//            emptyList()
//        }
//    }
//
//    suspend fun updateBookingStatus(bookingId: String, status: String): SimpleResponseDto {
//        return api.updateBookingStatus(bookingId, status)
//    }
//}