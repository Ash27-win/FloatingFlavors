package com.example.floatingflavors.app.feature.user.data.booking

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.BookingMenuItemDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.SaveBookingMenuRequestDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.SmartFilterRequestDto
import com.example.floatingflavors.app.feature.user.presentation.booking.SmartFilterState

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

    suspend fun fetchMenuBySmartFilter(
        state: SmartFilterState
    ): List<MenuItemDto> {
        val res = api.getMenuBySmartFilter(
            SmartFilterRequestDto(
                dietary = state.dietary.toList(),
                cuisines = state.cuisines.toList()
            )
        )
        if (!res.success) throw Exception("Filter failed")
        return res.data
    }

}
