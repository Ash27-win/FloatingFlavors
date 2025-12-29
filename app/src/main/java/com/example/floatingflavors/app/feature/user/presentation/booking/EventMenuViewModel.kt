package com.example.floatingflavors.app.feature.user.presentation.booking

import android.util.Log
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

    /**
     * 🔹 Called when EventMenuScreen opens
     * Restores menu + previously selected quantities from DB
     */
    fun loadMenuWithRestore(bookingId: Int) {
        viewModelScope.launch {
            try {
                // 1️⃣ Load menu
                val menu = repo.fetchMenu()

                // 2️⃣ Load saved selections
                val savedSelection = repo.fetchSavedMenuSelection(bookingId)

                // 3️⃣ Calculate total
                val total = savedSelection.entries.sumOf { (menuId, qty) ->
                    val item = menu.firstOrNull { it.idAsInt == menuId }
                    (item?.priceAsDouble ?: 0.0) * qty
                }

                // 4️⃣ Update UI
                _uiState.value = EventMenuUiState(
                    menu = menu,
                    selected = savedSelection,
                    totalAmount = total
                )
            } catch (e: Exception) {
                Log.e("LOAD_MENU", "Failed to load menu with restore", e)
            }
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

    //CHECKBOX LOGIC
    fun toggleItem(menuId: Int) {
        val selected = _uiState.value.selected.toMutableMap()

        if (selected.containsKey(menuId)) {
            // ❌ Checkbox UNCHECKED → remove item
            selected.remove(menuId)
        } else {
            // ✅ Checkbox CHECKED → add with qty = 1
            selected[menuId] = 1
        }

        recalc(selected)
    }


    private fun recalc(selected: Map<Int, Int>) {
        val total = selected.entries.sumOf { (id, qty) ->
            val menuItem = _uiState.value.menu.firstOrNull {
                it.id?.toIntOrNull() == id
            }
            (menuItem?.price?.toDoubleOrNull() ?: 0.0) * qty
        }

        _uiState.value = _uiState.value.copy(
            selected = selected,
            totalAmount = total
        )
    }

    /**
     * 🔹 Called ONLY when user clicks "Review Selection"
     */
    fun saveBookingMenu(bookingId: Int) {
        val items = _uiState.value.selected.mapNotNull { (menuId, quantity) ->
            val menuItem = _uiState.value.menu.firstOrNull {
                it.idAsInt == menuId
            } ?: return@mapNotNull null

            BookingMenuItemDto(
                menu_item_id = menuId,
                quantity = quantity,
                price = menuItem.priceAsDouble ?: 0.0
            )
        }

        if (items.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    repo.saveBookingMenu(bookingId, items)
                } catch (e: Exception) {
                    Log.e("SAVE_MENU", "Failed to save booking menu", e)
                }
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
