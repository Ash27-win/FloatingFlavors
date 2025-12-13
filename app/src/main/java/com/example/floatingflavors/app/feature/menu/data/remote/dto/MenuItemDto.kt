//package com.example.floatingflavors.app.feature.menu.data.remote.dto
//
//data class MenuItemDto(
//    val id: Int,
//    val name: String,
//    val description: String?,
//    val price: Double,
//    val category: String?,
//    val image_url: String?,   // must match PHP JSON key
//    val is_available: Int
//)

package com.example.floatingflavors.app.feature.menu.data.remote.dto

data class MenuItemDto(
    val id: String?,
    val name: String?,
    val description: String?,
    val price: String?,
    val category: String?,
    val image_url: String?,   // relative or legacy absolute
    val rating: Float? = null,   // ✅ ADD THIS
    val image_full: String?,  // normalized absolute (from backend)
    val is_available: String?,
    val created_at: String?
)




