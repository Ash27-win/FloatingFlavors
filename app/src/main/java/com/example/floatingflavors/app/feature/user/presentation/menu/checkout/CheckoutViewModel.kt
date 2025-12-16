package com.example.floatingflavors.app.feature.user.presentation.menu.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.cart.CheckoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CheckoutUiState {
    object Loading : CheckoutUiState()
    data class Success(val items: List<CheckoutItemUi>, val total: Int) : CheckoutUiState()
    data class Error(val msg: String) : CheckoutUiState()
    object Placed : CheckoutUiState()
}

data class CheckoutItemUi(
    val name: String,
    val qty: Int,
    val price: Int
)

class CheckoutViewModel(
    private val repo: CheckoutRepository = CheckoutRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
    val uiState: StateFlow<CheckoutUiState> = _uiState

    val payment = MutableStateFlow("upi")

    // ✅ NOW ACCEPTS userId
    fun load(userId: Int) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState.Loading

            val res = repo.getSummary(userId)

            if (res.isSuccessful && res.body()?.success == true) {
                val data = res.body()!!.data!!

                _uiState.value = CheckoutUiState.Success(
                    items = data.items.map {
                        CheckoutItemUi(
                            name = it.name,
                            qty = it.quantity,
                            price = it.subtotal
                        )
                    },
                    total = data.total
                )
            } else {
                _uiState.value = CheckoutUiState.Error("Failed to load checkout")
            }
        }
    }

    // ✅ NOW ACCEPTS userId
    fun place(userId: Int) {
        viewModelScope.launch {
            val res = repo.placeOrder(userId, payment.value)

            if (res.isSuccessful && res.body()?.success == true) {
                _uiState.value = CheckoutUiState.Placed
            } else {
                _uiState.value = CheckoutUiState.Error("Order failed")
            }
        }
    }
}






//package com.example.floatingflavors.app.feature.user.presentation.menu.checkout
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.floatingflavors.app.feature.user.data.cart.CheckoutRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//sealed class CheckoutUiState {
//    object Loading : CheckoutUiState()
//    data class Success(val items: List<CheckoutItemUi>, val total: Int) : CheckoutUiState()
//    data class Error(val msg: String) : CheckoutUiState()
//    object Placed : CheckoutUiState()
//}
//
//data class CheckoutItemUi(
//    val name: String,
//    val qty: Int,
//    val price: Int
//)
//
//class CheckoutViewModel(
//    private val repo: CheckoutRepository = CheckoutRepository()
//) : ViewModel() {
//
//    val uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
//    val payment = MutableStateFlow("upi")
//
//    fun load(userId: Int) {
//        viewModelScope.launch {
//            uiState.value = CheckoutUiState.Loading
//            val res = repo.getSummary(userId)
//            if (res.isSuccessful && res.body()?.success == true) {
//                val d = res.body()!!.data!!
//                uiState.value = CheckoutUiState.Success(
//                    d.items.map { CheckoutItemUi(it.name, it.quantity, it.subtotal) },
//                    d.total
//                )
//            } else {
//                uiState.value = CheckoutUiState.Error("Failed to load checkout")
//            }
//        }
//    }
//
//    fun place(userId: Int) {
//        viewModelScope.launch {
//            val res = repo.placeOrder(userId, payment.value)
//            if (res.isSuccessful && res.body()?.success == true) {
//                uiState.value = CheckoutUiState.Placed
//            } else {
//                uiState.value = CheckoutUiState.Error("Order failed")
//            }
//        }
//    }
//}
