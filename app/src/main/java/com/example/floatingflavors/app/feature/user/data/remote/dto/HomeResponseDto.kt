//package com.example.floatingflavors.app.feature.user.data.remote.dto
//
//import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
//
//data class HomeApiResponse(
//    val status: String,
//    val message: String?,
//    val data: HomeDataDto?
//)
//
//data class HomeDataDto(
//    val userStats: UserStatsDto,
//    val featured: List<MenuItemDto>,
//    val offer: OfferDto?
//)
//
//data class UserStatsDto(
//    val userId: Int,
//    val userName: String,
//    val totalOrders: Int,
//    val loyaltyPoints: Int
//)
//
//data class OfferDto(
//    val title: String,
//    val subtitle: String
//)





package com.example.floatingflavors.app.feature.user.data.remote.dto

import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto

// This is the DTO shape used by ViewModel (HomeResponseDto) referenced in your ViewModel.
// It simplifies ApiHomeResponse into the fields used by the UI.

data class HomeResponseDto(
    val userStats: UserStats,
    val featured: List<MenuItemDto>,
    val offer: OfferDto?
)

data class UserStats(
    val userId: Int,
    val userName: String,
    val totalOrders: Int,
    val loyaltyPoints: Int
)

data class OfferDto(
    val title: String?,
    val subtitle: String?,
    val ctaText: String?
)
