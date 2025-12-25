package com.example.floatingflavors.app.feature.user.presentation.booking.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.orders.data.OrdersRepository
import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserBookingStatusViewModel(
    private val repo: OrdersRepository = OrdersRepository()
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<AdminBookingDto>>(emptyList())
    val bookings: StateFlow<List<AdminBookingDto>> = _bookings

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun loadUserBookings() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _bookings.value = repo.getEventBookings()
            } finally {
                _loading.value = false
            }
        }
    }
}
