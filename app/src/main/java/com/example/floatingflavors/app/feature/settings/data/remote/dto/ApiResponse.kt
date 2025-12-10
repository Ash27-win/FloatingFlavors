package com.example.floatingflavors.app.feature.admin.data.remote.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T?
)
