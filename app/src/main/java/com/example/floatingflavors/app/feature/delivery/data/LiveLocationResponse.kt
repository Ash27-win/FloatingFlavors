package com.example.floatingflavors.app.feature.delivery.data

import com.example.floatingflavors.app.feature.delivery.data.remote.LiveLocationData
import com.google.gson.annotations.SerializedName

data class LiveLocationResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("location") val location: LiveLocationData? = null
)