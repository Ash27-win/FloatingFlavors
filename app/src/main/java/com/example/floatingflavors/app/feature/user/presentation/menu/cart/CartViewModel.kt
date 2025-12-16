package com.example.floatingflavors.app.feature.user.presentation.menu.cart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.cart.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CartViewModel(
    private val repo: CartRepository = CartRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState: StateFlow<CartUiState> = _uiState

    fun loadCart(userId: Int) {
        if (userId <= 0) {
            Log.e("CART", "loadCart blocked: userId=0")
            _uiState.value = CartUiState.Error("User not logged in")
            return
        }

        viewModelScope.launch {
            val response = repo.fetchCart(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                _uiState.value = CartUiState.Success(
                    items = response.body()!!.items, // ✅ DTO 그대로
                    total = response.body()!!.total
                )
            } else {
                _uiState.value = CartUiState.Error("Failed to load cart")
            }
        }
    }

    fun add(userId: Int, menuId: Int, price: Int) {
        if (userId <= 0) return
        viewModelScope.launch {
            repo.addItem(userId, menuId, price)
            loadCart(userId)
        }
    }

    fun increase(userId: Int, itemId: Int) {
        if (userId <= 0) return
        viewModelScope.launch {
            repo.increase(itemId)
            loadCart(userId)
        }
    }

    fun decrease(userId: Int, itemId: Int) {
        if (userId <= 0) return
        viewModelScope.launch {
            repo.decrease(itemId)
            loadCart(userId)
        }
    }

    fun remove(userId: Int, itemId: Int) {
        if (userId <= 0) return
        viewModelScope.launch {
            repo.remove(itemId)
            loadCart(userId)
        }
    }
}






//package com.example.floatingflavors.app.feature.user.presentation.menu.cart
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.floatingflavors.app.feature.user.data.cart.CartRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//class CartViewModel(
//    private val repo: CartRepository = CartRepository()
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
//    val uiState: StateFlow<CartUiState> = _uiState
//
//    fun loadCart(userId: Int) {
//        Log.d("CART_DEBUG", "Loading cart for userId=$userId")
//        viewModelScope.launch {
//            if (userId <= 0) {
//                _uiState.value = CartUiState.Error("User not logged in")
//                return@launch
//            }
//
//            val response = repo.fetchCart(userId)
//            if (response.isSuccessful && response.body()?.success == true) {
//                _uiState.value = CartUiState.Success(
//                    response.body()!!.items,
//                    response.body()!!.total
//                )
//            } else {
//                _uiState.value = CartUiState.Error("Failed to load cart")
//            }
//        }
//    }
//
//    fun add(userId: Int, menuId: Int, price: Int) {
//        viewModelScope.launch {
//            repo.addItem(userId, menuId, price)
//            loadCart(userId)
//        }
//    }
//
//    fun increase(userId: Int, itemId: Int) {
//        viewModelScope.launch {
//            repo.increase(itemId)
//            loadCart(userId)
//        }
//    }
//
//    fun decrease(userId: Int, itemId: Int) {
//        viewModelScope.launch {
//            repo.decrease(itemId)
//            loadCart(userId)
//        }
//    }
//
//    fun remove(userId: Int, itemId: Int) {
//        viewModelScope.launch {
//            repo.remove(itemId)
//            loadCart(userId)
//        }
//    }
//}
//
//
//
//
//// 16/12/25 USERSESSION ERROR
//
////class CartViewModel(
////    private val repo: CartRepository = CartRepository()
////) : ViewModel() {
////
////    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
////    val uiState: StateFlow<CartUiState> = _uiState
////
////    private val userId = 1   // later from login
////
////    fun loadCart() {
////        viewModelScope.launch {
////            val response = repo.fetchCart(userId)
////            if (response.isSuccessful && response.body()?.success == true) {
////                _uiState.value = CartUiState.Success(
////                    response.body()!!.items,
////                    response.body()!!.total
////                )
////            } else {
////                _uiState.value = CartUiState.Error("Failed to load cart")
////            }
////        }
////    }
////
////    fun add(menuId: Int, price: Int) {
////        viewModelScope.launch {
////            repo.addItem(userId, menuId, price)
////            loadCart()
////        }
////    }
////
////    fun increase(itemId: Int) {
////        viewModelScope.launch {
////            repo.increase(itemId)
////            loadCart()
////        }
////    }
////
////    fun decrease(itemId: Int) {
////        viewModelScope.launch {
////            repo.decrease(itemId)
////            loadCart()
////        }
////    }
////
////    fun remove(itemId: Int) {
////        viewModelScope.launch {
////            repo.remove(itemId)
////            loadCart()
////        }
////    }
////}
