package com.example.floatingflavors.app.feature.delivery.data

import com.google.gson.annotations.SerializedName

// Response data classes
data class DeliveryOrderDetailsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: DeliveryOrderData? = null
)