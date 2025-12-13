package com.example.floatingflavors.app.feature.user.data.remote.dto

import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.google.gson.annotations.SerializedName

// Matches the JSON returned by get_user_home.php
data class ApiHomeResponse(
    val status: String?,
    val message: String?,
    val data: HomeResponseDto?   // ✅ NOT ApiHomeData
)


data class ApiHomeData(
    val user: ApiUserDto?,
    @SerializedName("total_orders")
    val totalOrders: Int = 0,
    @SerializedName("loyalty_points")
    val loyaltyPoints: Int = 0,
    @SerializedName("featured_items")
    val featuredItems: List<MenuItemDto> = emptyList(), // reuse existing MenuItemDto
    @SerializedName("quick_actions")
    val quickActions: List<ApiQuickActionDto> = emptyList(),
    val banner: ApiBannerDto? = null
)

data class ApiUserDto(
    val id: Int,
    val name: String?,
    val email: String?,
    @SerializedName("loyalty_points")
    val loyaltyPoints: Int? = 0
)

data class ApiQuickActionDto(
    val id: String?,
    val title: String?,
    val icon: String?
)

data class ApiBannerDto(
    val title: String?,
    val subtitle: String?,
    val cta: String?
)
