package com.example.floatingflavors.app.feature.user.presentation.booking

import com.example.floatingflavors.app.feature.user.data.booking.dto.BookingEventInfoDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.ReviewDishDto

data class ReviewUiState(
    val loading: Boolean = true,
    val event: BookingEventInfoDto? = null,
    val items: List<ReviewDishDto> = emptyList(),
    val total: Double = 0.0
)
