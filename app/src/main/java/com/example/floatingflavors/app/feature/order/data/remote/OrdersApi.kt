package com.example.floatingflavors.app.feature.orders.data.remote

import com.example.floatingflavors.app.feature.menu.data.remote.dto.SimpleResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingResponse
import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingsListResponse
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrderDetailResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersCountsResponseDto
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersServerResponseDto
import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrdersResponseDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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

    // Form-encoded (works with PHP $_POST)
    @FormUrlEncoded
    @POST("update_order_status.php")
    suspend fun updateOrderStatus(
        @Field("order_id") orderId: Int,
        @Field("status") status: String
    ): SimpleResponseDto

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
