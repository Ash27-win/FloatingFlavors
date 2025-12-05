//package com.example.floatingflavors.app.feature.menu.data.remote.dto
//
//data class MenuResponseDto(
//    val success: Boolean,
//    val message: String,
//    val data: List<MenuItemDto>?
//)
package com.example.floatingflavors.app.feature.menu.data.remote.dto

data class MenuResponseDto(
    val success: Boolean = false,
    val message: String? = null,
    val data: List<MenuItemDto>? = emptyList()
)
