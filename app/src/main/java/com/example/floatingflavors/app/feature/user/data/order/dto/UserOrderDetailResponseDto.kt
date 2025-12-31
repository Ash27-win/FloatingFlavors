package com.example.floatingflavors.app.feature.user.data.order.dto

import com.example.floatingflavors.app.feature.user.data.order.dto.UserOrderDetailDto

data class UserOrderDetailResponseDto(
    val success: Boolean,
    val message: String?,
    val data: UserOrderDetailDto?
)
