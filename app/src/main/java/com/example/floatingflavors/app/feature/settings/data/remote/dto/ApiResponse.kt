package com.example.floatingflavors.app.feature.settings.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName(value = "status", alternate = ["success"])
    val status: Boolean,

    val message: String,

    val data: T?
)
