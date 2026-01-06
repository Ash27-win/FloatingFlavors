package com.example.floatingflavors.app.feature.delivery.data

import com.google.gson.annotations.SerializedName

data class OrderDto(
    val id: Int,
    val customerName: String,
    val pickupAddress: String,
    val dropAddress: String,
    val amount: Int,
    val status: String? = null  // Add this field!
)
