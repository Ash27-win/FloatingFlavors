package com.example.floatingflavors.app.feature.user.presentation.booking

import android.R.attr.id
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.menu.data.remote.dto.idAsInt
import com.example.floatingflavors.app.feature.menu.data.remote.dto.priceAsDouble
import com.example.floatingflavors.app.feature.user.data.booking.BookingMenuRepository
import com.example.floatingflavors.app.feature.user.data.booking.dto.BookingMenuItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventMenuViewModel(
    private val repo: BookingMenuRepository = BookingMenuRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventMenuUiState())
    val uiState: StateFlow<EventMenuUiState> = _uiState

    fun loadMenu() {
        viewModelScope.launch {
            val menu = repo.fetchMenu()
            _uiState.value = _uiState.value.copy(menu = menu)
        }
    }

    fun addItem(menuId: Int) {
        val selected = _uiState.value.selected.toMutableMap()
        selected[menuId] = (selected[menuId] ?: 0) + 1
        recalc(selected)
    }

    fun removeItem(menuId: Int) {
        val selected = _uiState.value.selected.toMutableMap()
        val qty = (selected[menuId] ?: 0) - 1
        if (qty <= 0) selected.remove(menuId) else selected[menuId] = qty
        recalc(selected)
    }

    private fun recalc(selected: Map<Int, Int>) {
        val total = selected.entries.sumOf { (id, qty) ->
            // Find the menu item and convert String? price to Double safely
            val menuItem = _uiState.value.menu.firstOrNull {
                it.id?.toIntOrNull() == id
            }

            val price = menuItem?.price?.toDoubleOrNull() ?: 0.0
            price * qty
        }

        _uiState.value = _uiState.value.copy(
            selected = selected,
            totalAmount = total
        )
    }

    fun saveBookingMenu(bookingId: Int) {
        val items = _uiState.value.selected.mapNotNull { (menuId, quantity) ->
            // Find the menu item
            val menuItem = _uiState.value.menu.firstOrNull {
                it.idAsInt == id
            }
            val price = menuItem?.priceAsDouble ?: 0.0

            // Convert menuId to Int for BookingMenuItemDto
            val menuItemId = menuItem?.id?.toIntOrNull() ?: return@mapNotNull null

            BookingMenuItemDto(
                menu_item_id = menuItemId,
                quantity = quantity,
                price = price
            )
        }

        if (items.isNotEmpty()) {
            viewModelScope.launch {
                repo.saveBookingMenu(bookingId, items)
            }
        }
    }

    fun applySmartFilter(state: SmartFilterState) {
        viewModelScope.launch {
            val filtered = repo.fetchMenuBySmartFilter(state)
            _uiState.value = _uiState.value.copy(menu = filtered)
        }
    }


}