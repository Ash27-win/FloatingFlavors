package com.example.floatingflavors.app.feature.delivery.data

import com.google.gson.annotations.SerializedName

data class LiveLocationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("location") val location: LiveLocationData? = null
)

data class LiveLocationData(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double
)
