package com.example.floatingflavors.app.feature.delivery.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeliveryProfileResponse(
    val success: Boolean,
    val profile: DeliveryProfileDto?,
    val message: String?
)

data class DeliveryProfileDto(
    val name: String,
    val email: String,
    val phone: String,
    @SerializedName("emergency_contact") val emergencyContact: String,
    @SerializedName("profile_image") val profileImage: String,
    val rating: Double,
    val tier: String,
    val vehicle: VehicleDto,
    val stats: StatsDto,
    @SerializedName("is_verified") val isVerified: Boolean
)

data class VehicleDto(
    val type: String,
    val number: String
)

data class StatsDto(
    @SerializedName("total_deliveries") val totalDeliveries: Int,
    @SerializedName("joined_at") val joinedAt: String
)

data class UpdateDeliveryProfileResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("image_url") val imageUrl: String?
)
