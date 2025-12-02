package com.example.floatingflavors.app.feature.menu.data.remote.dto

data class MenuItemDto(
    val id: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val category: String?,
    val image_url: String?,   // must match PHP JSON key
    val is_available: Int
)