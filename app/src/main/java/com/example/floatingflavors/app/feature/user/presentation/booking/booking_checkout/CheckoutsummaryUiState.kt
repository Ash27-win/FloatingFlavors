package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import com.example.floatingflavors.app.feature.user.data.booking_checkout.dto.BookingEventDto
import com.example.floatingflavors.app.feature.user.data.booking_checkout.dto.BookingItemDto
import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto

data class CheckoutSummaryUiState(
    val loading: Boolean = true,

    val address: AddressDto? = null,
    val event: BookingEventDto? = null,
    val items: List<BookingItemDto> = emptyList(),

    val subtotal: Double = 0.0,
    val gst: Double = 0.0,
    val deliveryFee: Double = 20.0,
    val total: Double = 0.0
)
