package com.example.floatingflavors.app.feature.user.data.booking.dto

import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto

data class MenuBookingResponseDto(
    val success: Boolean,
    val data: List<MenuItemDto>
)
