package com.example.floatingflavors.app.feature.auth.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateFcmTokenRequest(
    @SerializedName("fcm_token") val fcmToken: String,
    @SerializedName("device_info") val deviceInfo: String = "Android Device"
)
