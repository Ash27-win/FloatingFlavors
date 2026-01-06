// In OrderDto.kt (or wherever OrderDto is defined)
package com.example.floatingflavors.app.feature.orders.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("customer_name") val customer_name: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("time_ago") val time_ago: String? = null,
    @SerializedName("items") val items: List<OrdersItemDto>? = null,
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("distance") val distance: String? = null,

    // ✅ ADD THIS FIELD
    @SerializedName("delivery_partner_id") val delivery_partner_id: String? = null,

    // For bookings
    val isBooking: Boolean = false,
    val bookingId: String? = null
)





//// OrderDto.kt
//package com.example.floatingflavors.app.feature.orders.data.remote.dto
//
//data class OrderDto(
//    val id: String?,
//    val customer_name: String?,
//    val items: List<OrdersItemDto>?,
//    val status: String?,
//    val created_at: String?,   // ISO 8601 string from backend
//    val time_ago: String?,
//    val distance: String?,
//    val amount: String?,
//    val isBooking: Boolean = false,
//    val bookingId: String? = null
//)
