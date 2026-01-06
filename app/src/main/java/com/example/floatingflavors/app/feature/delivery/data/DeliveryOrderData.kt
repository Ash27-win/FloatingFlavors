package com.example.floatingflavors.app.feature.delivery.data

import com.google.gson.annotations.SerializedName

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



//data class DeliveryOrderData(
//    val id: String,
//    val customer_name: String?,
//    val status: String?,
//    val amount: String?,
//    val delivery_partner_id: String?,
//    val customer_phone: String? = null,
//    val delivery_address: String? = null
//)