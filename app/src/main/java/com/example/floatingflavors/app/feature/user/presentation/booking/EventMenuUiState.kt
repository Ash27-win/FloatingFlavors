package com.example.floatingflavors.app.feature.user.presentation.booking

import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto

data class EventMenuUiState(
    val menu: List<MenuItemDto> = emptyList(),
    val selected: Map<Int, Int> = emptyMap(),
    val totalAmount: Double = 0.0
)
