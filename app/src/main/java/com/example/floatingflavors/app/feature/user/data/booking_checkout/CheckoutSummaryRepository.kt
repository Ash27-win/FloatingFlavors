package com.example.floatingflavors.app.feature.user.data.booking_checkout

import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto

class CheckoutSummaryRepository(
    private val api: CheckoutSummaryApi
) {

    suspend fun loadSummary(bookingId: Int) =
        api.getBookingSummary(bookingId)

    suspend fun loadAddress(userId: Int, addressId: Int): AddressDto? {
        val res = api.getAddresses(userId)
        return res.data.firstOrNull { it.id == addressId }
    }
}
