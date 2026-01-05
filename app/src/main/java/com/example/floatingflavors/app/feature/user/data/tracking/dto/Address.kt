package com.example.floatingflavors.app.feature.user.data.tracking.dto

data class Address(
    val line1: String,
    val city: String,
    val pincode: String,
    val note: String,
    val latitude: Double,
    val longitude: Double
)