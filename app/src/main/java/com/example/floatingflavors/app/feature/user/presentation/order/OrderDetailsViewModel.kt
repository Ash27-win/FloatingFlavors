package com.example.floatingflavors.app.feature.user.presentation.order

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.order.UserOrdersRepository
import com.example.floatingflavors.app.feature.user.data.settings.AddressRepository
import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto
import com.example.floatingflavors.app.feature.user.data.order.dto.UserOrderDetailDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderDetailsViewModel(
    private val ordersRepo: UserOrdersRepository,
    private val userId: Int
) : ViewModel() {

    private val _order = MutableStateFlow<UserOrderDetailDto?>(null)
    val order: StateFlow<UserOrderDetailDto?> = _order

    fun load(orderId: String) {
        viewModelScope.launch {
            _order.value = ordersRepo.getOrderDetailsScreen(orderId)
        }
    }
}
