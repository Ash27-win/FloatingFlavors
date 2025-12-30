package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.booking_checkout.CheckoutSummaryRepository
import kotlinx.coroutines.launch

class CheckoutSummaryViewModel(
    private val repo: CheckoutSummaryRepository
) : ViewModel() {

    var uiState by mutableStateOf(CheckoutSummaryUiState())
        private set

    fun load(userId: Int, bookingId: Int, addressId: Int) {
        viewModelScope.launch {
            try {
                val summary = repo.loadSummary(bookingId)
                val address = repo.loadAddress(userId, addressId)

                val subtotal = summary.items.sumOf { it.total.toDouble() }
                val gst = subtotal * 0.05
                val total = subtotal + gst + uiState.deliveryFee

                uiState = uiState.copy(
                    loading = false,
                    address = address,
                    event = summary.event,
                    items = summary.items,
                    subtotal = subtotal,
                    gst = gst,
                    total = total
                )
            } catch (e: Exception) {
                uiState = uiState.copy(loading = false)
            }
        }
    }
}
