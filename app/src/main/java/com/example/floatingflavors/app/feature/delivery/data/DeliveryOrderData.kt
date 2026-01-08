package com.example.floatingflavors.app.feature.delivery.data

import com.google.gson.annotations.SerializedName

data class DeliveryOrderData(
    val id: String?,

    @SerializedName("customer_name")
    val customerName: String?,

    val status: String?,
    val amount: String?,

    @SerializedName("delivery_partner_id")
    val deliveryPartnerId: String?,

    val items: List<OrderItemDto>?,
    val distance: String?,

    @SerializedName("created_at")
    val createdAt: String?,

    // 🔥 THIS WAS MISSING — FINAL FIX
    @SerializedName("delivery_address")
    val deliveryAddress: String?
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