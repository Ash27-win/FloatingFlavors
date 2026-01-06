// REPLACE OrdersApi.kt with this:
package com.example.floatingflavors.app.feature.orders.data.remote

import com.example.floatingflavors.app.feature.menu.data.remote.dto.SimpleResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingResponse
import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingsListResponse
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrderDetailResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrderStatusUpdateResponse
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersCountsResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersServerResponseDto
import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrdersResponseDto
import com.example.floatingflavors.app.feature.settings.data.remote.dto.ApiResponse
import okhttp3.RequestBody
import retrofit2.http.*

interface OrdersApi {
    @GET("get_orders.php")
    suspend fun getOrders(): OrdersResponseDto

    @GET("get_order_detail.php")
    suspend fun getOrderDetail(@Query("id") id: Int): OrderDetailResponseDto

    @GET("get_orders_server.php")
    suspend fun getOrdersServer(
        @Query("query") query: String? = null,
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): OrdersServerResponseDto

    @GET("get_orders_counts.php")
    suspend fun getOrdersCounts(): OrdersCountsResponseDto

    // ✅ FIXED: Accept RequestBody instead of MultipartBody.Part
    @POST("update_order_status.php")
    suspend fun updateOrderStatus(
        @Body body: RequestBody
    ): ApiResponse<OrderStatusUpdateResponse>

    // 🔥 BOOKINGS
    @GET("get_event_bookings.php")
    suspend fun getEventBookings(): AdminBookingsListResponse

    @FormUrlEncoded
    @POST("update_booking_status.php")
    suspend fun updateBookingStatus(
        @Field("booking_id") bookingId: String,
        @Field("status") status: String
    ): SimpleResponseDto
}