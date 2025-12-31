package com.example.floatingflavors.app.feature.user.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.orders.data.OrdersRepository
import com.example.floatingflavors.app.feature.user.data.booking.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserOrdersViewModel(
    private val ordersRepository: OrdersRepository,
    private val bookingRepository: BookingRepository,
    private val userId: Int
) : ViewModel() {

    private val _activeOrders = MutableStateFlow<List<UserOrderUiModel>>(emptyList())
    val activeOrders: StateFlow<List<UserOrderUiModel>> = _activeOrders

    private val _pastOrders = MutableStateFlow<List<UserOrderUiModel>>(emptyList())
    val pastOrders: StateFlow<List<UserOrderUiModel>> = _pastOrders

    init {
        loadUserOrders()
    }

    private fun loadUserOrders() {
        viewModelScope.launch {

            val active = mutableListOf<UserOrderUiModel>()
            val past = mutableListOf<UserOrderUiModel>()

            /* ---------- NORMAL ORDERS ---------- */
            ordersRepository.getOrders().data.orEmpty().forEach { order ->
                val ui = UserOrderUiModel(
                    orderId = order.id ?: "",
                    dateTime = order.created_at ?: "",
                    items = order.items.orEmpty().map {
                        "${it.qty ?: 0} x ${it.name.orEmpty()}"
                    },
                    status = order.status ?: "Pending",
                    amount = "₹${order.amount ?: "0"}",
                    isEvent = false
                )

                when (ui.status.lowercase()) {
                    "pending", "preparing", "confirmed" -> active.add(ui)
                    else -> past.add(ui)
                }
            }

            /* ---------- EVENT BOOKING ---------- */
            bookingRepository.getUserActiveBooking(userId)?.let { booking ->
                val bookingUi = UserOrderUiModel(
                    orderId = "CAT${booking.booking_id}",
                    dateTime = booking.event_date ?: "",
                    items = listOf(
                        "${booking.people_count ?: 0} x ${booking.event_type.orEmpty()} Catering Pack"
                    ),
                    status = booking.status ?: "Confirmed",
                    amount = "0",
                    isEvent = true
                )

                if (bookingUi.status.lowercase() == "completed") {
                    past.add(bookingUi)
                } else {
                    active.add(bookingUi)
                }
            }

            _activeOrders.value = active
            _pastOrders.value = past
        }
    }
}
