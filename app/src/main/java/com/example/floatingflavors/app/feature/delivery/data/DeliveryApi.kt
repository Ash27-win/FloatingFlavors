package com.example.floatingflavors.app.feature.delivery.data.remote

import com.example.floatingflavors.app.feature.delivery.data.DeliveryOrderDetailsResponse
import com.example.floatingflavors.app.feature.delivery.data.DeliveryStatusResponse
import com.example.floatingflavors.app.feature.delivery.data.LiveLocationResponse
import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface DeliveryApi {
    // ✅ REUSE: For getting order details, use get_order_detail.php (already exists)
    @GET("get_order_detail.php")
    suspend fun getOrderDetails(
        @Query("id") orderId: Int
    ): DeliveryOrderDetailsResponse

    // ✅ REUSE: For updating order status, use update_order_status.php (already exists)
    @FormUrlEncoded
    @POST("update_order_status.php")
    suspend fun updateOrderStatus(
        @Field("order_id") orderId: Int,
        @Field("status") status: String,
        @Field("delivery_partner_id") deliveryPartnerId: Int? = null
    ): SimpleResponseDto

    // ✅ REUSE: For updating live location, use update_order_location.php (already exists)
    @FormUrlEncoded
    @POST("update_order_location.php")
    suspend fun updateLiveLocation(
        @Field("order_id") orderId: Int,
        @Field("lat") lat: Double,
        @Field("lng") lng: Double,
        @Field("delivery_partner_id") deliveryPartnerId: Int
    ): SimpleResponseDto

    // ✅ REUSE: For getting live location, use get_order_location.php (already exists)
    @GET("get_order_location.php")
    suspend fun getLiveLocation(
        @Query("order_id") orderId: Int
    ): LiveLocationResponse

    // ✅ REUSE: For order tracking details, use order_tracking_details.php (already exists)
    @GET("order_tracking_details.php")
    suspend fun getOrderTracking(
        @Query("order_id") orderId: Int,
        @Query("type") type: String = "INDIVIDUAL"
    ): OrderTrackingResponse

    // ✅ NEW: For delivery dashboard (shows orders assigned to this delivery partner)
    @GET("delivery_dashboard.php")
    suspend fun getDashboard(
        @Query("delivery_partner_id") deliveryPartnerId: Int
    ): DeliveryDashboardResponse
}

// Response data classes
data class DeliveryOrderDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: DeliveryOrderData? = null
)

data class DeliveryOrderData(
    @SerializedName("id") val id: String?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("amount") val amount: String?,
    @SerializedName("delivery_partner_id") val deliveryPartnerId: String?,
    @SerializedName("items") val items: List<OrderItemDto>? = null,
    @SerializedName("time_ago") val timeAgo: String? = null,
    @SerializedName("distance") val distance: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class OrderItemDto(
    @SerializedName("name") val name: String?,
    @SerializedName("qty") val qty: Int?
)

data class DeliveryStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)

data class LiveLocationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("location") val location: LiveLocationData? = null
)

data class LiveLocationData(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class OrderTrackingResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("currentStatus") val currentStatus: String? = null,
    @SerializedName("deliveryLocation") val deliveryLocation: DeliveryLocationData? = null,
    @SerializedName("deliveryPerson") val deliveryPerson: DeliveryPersonData? = null,
    @SerializedName("deliveryAddress") val deliveryAddress: DeliveryAddressData? = null
)

data class DeliveryLocationData(
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)

data class DeliveryPersonData(
    @SerializedName("name") val name: String?,
    @SerializedName("vehicle") val vehicle: String?
)

data class DeliveryAddressData(
    @SerializedName("line1") val line1: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("pincode") val pincode: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)

data class DeliveryDashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("delivery_partner_name") val deliveryPartnerName: String? = null,
    @SerializedName("active_order") val activeOrder: ActiveOrderDto? = null,
    @SerializedName("upcoming_orders") val upcomingOrders: List<UpcomingOrderDto>? = null
)

data class ActiveOrderDto(
    @SerializedName("id") val id: String?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("amount") val amount: String?,
    @SerializedName("delivery_partner_id") val deliveryPartnerId: String?
)

data class UpcomingOrderDto(
    @SerializedName("id") val id: String?,
    @SerializedName("customer_name") val customerName: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("amount") val amount: String?,
    @SerializedName("pickup_address") val pickupAddress: String?,
    @SerializedName("drop_address") val dropAddress: String?
)

// Simple response DTO (reuse from existing code)
data class SimpleResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)



//import com.example.floatingflavors.app.feature.auth.data.remote.dto.SimpleResponseDto
//import com.example.floatingflavors.app.feature.delivery.data.DeliveryDashboardResponseDto
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.POST
//import retrofit2.http.Query
//
//interface DeliveryApi {
//
//    // 🔹 DASHBOARD (active + upcoming)
//    @GET("delivery_dashboard.php")
//    suspend fun getDashboard(
//        @Query("delivery_partner_id") deliveryPartnerId: Int
//    ): DeliveryDashboardResponseDto
//
//    // 🔹 UPDATE STATUS (REUSE YOUR EXISTING FILE)
//    @POST("update_order_status.php")
//    suspend fun updateOrderStatus(
//        @Body body: UpdateOrderStatusRequestDto
//    ): SimpleResponseDto
//}
