package com.example.floatingflavors.app.feature.delivery.data

import com.google.gson.annotations.SerializedName
data class DeliveryStatusResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null
)