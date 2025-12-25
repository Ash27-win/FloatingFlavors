package com.example.floatingflavors.app.feature.user.data.booking

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.BookingMenuItemDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.SaveBookingMenuRequestDto

class BookingMenuRepository {

    // ✅ USE EXISTING CLIENT
    private val api = NetworkClient.bookingApi

    suspend fun fetchMenu(): List<MenuItemDto> {
        val res = api.getMenuForBooking()
        if (!res.success) throw Exception("Menu load failed")
        return res.data
    }

    suspend fun saveBookingMenu(
        bookingId: Int,
        items: List<BookingMenuItemDto>
    ) {
        api.saveBookingMenu(
            SaveBookingMenuRequestDto(bookingId, items)
        )
    }
}
