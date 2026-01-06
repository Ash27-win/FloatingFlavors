package com.example.floatingflavors.app.feature.delivery.data

import com.google.gson.annotations.SerializedName
data class OrderItemDto(
    @SerializedName("name") val name: String?,
    @SerializedName("qty") val qty: Int?
)