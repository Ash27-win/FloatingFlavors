package com.example.floatingflavors.app.chatbot.data

import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.google.gson.annotations.SerializedName

data class ChatBotResponse(
    val reply: String? = null,
    val type: String? = null,

    // MENU
    val menu_items: List<MenuItemDto>? = null,

    // ORDER HISTORY
    val orders: List<OrderDto>? = null,

    // EVENT HISTORY
    val events: List<EventDto>? = null,

    // CORPORATE HISTORY
    val corporate_bookings: List<CorporateDto>? = null,

    val cta: String? = null,
    val title: String? = null
)


data class MenuItem(
    val id: Int,
    val name: String,
    val description: String,
    val price: Int,
    val category: String,

    @SerializedName("image_url")
    val imageUrl: String?
)




//package com.example.floatingflavors.app.chatbot.data
//
//data class ChatResponse(
//    val reply: String
//)
