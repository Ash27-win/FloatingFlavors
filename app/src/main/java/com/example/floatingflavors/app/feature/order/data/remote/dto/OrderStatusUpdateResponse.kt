package com.example.floatingflavors.app.feature.order.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderStatusUpdateResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("order_id") val orderId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String? = null
)