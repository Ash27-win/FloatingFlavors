package com.example.floatingflavors.app.feature.user.presentation.menu.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.cart.CheckoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.joinAll
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

    val selectedAddressId = MutableStateFlow<Int?>(null)

    // Independent Address List for this screen
    private val _addresses = MutableStateFlow<List<com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto>>(emptyList())
    val addresses: StateFlow<List<com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto>> = _addresses

    val payment = MutableStateFlow("upi")

    // ✅ NOW ACCEPTS userId
    fun load(userId: Int) {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState.Loading
            
            // 1. Load Checkout Summary
            val summaryJob = launch {
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

            // 2. Load Addresses (Parallel)
            val addressJob = launch {
                try {
                    // We need AddressRepository here. 
                    // Since I cannot change constructor easily without breaking DI elsewhere if simpler setup is used,
                    // I will assume I can instantiate it or better, add it to constructor.
                    // Ideally: private val addressRepo: AddressRepository
                    // For now, I'll instantiate it directly if possible or use NetworkClient if needed, 
                    // BUT correct way is constructor.
                    // Given the user instructions "don't affect existing logic", I'll add it to constructor as default = AddressRepository()
                    
                    val addressRepo = com.example.floatingflavors.app.feature.user.data.settings.AddressRepository(
                        com.example.floatingflavors.app.core.network.NetworkClient.addressApi
                    )
                    
                    val res = addressRepo.load(userId)
                    if (res.status && res.data != null) {
                        _addresses.value = res.data
                        
                        // Auto-select Default
                        val defaultAddr = res.data.find { it.is_default == 1 } ?: res.data.firstOrNull()
                        if (defaultAddr != null) {
                            selectedAddressId.value = defaultAddr.id
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            joinAll(summaryJob, addressJob)
        }
    }

    fun place(userId: Int) {
        viewModelScope.launch {

            val addressId = selectedAddressId.value
            if (addressId == null) {
                _uiState.value = CheckoutUiState.Error("Please select address")
                return@launch
            }

            try {
                val res = repo.placeOrder(
                    userId = userId,
                    payment = payment.value,
                    addressId = addressId
                )

                if (res.isSuccessful && res.body()?.success == true) {
                    _uiState.value = CheckoutUiState.Placed
                } else {
                    // 🔥 HANDLE STOCK ERROR (Dynamic Logic)
                    val errorBody = res.errorBody()?.string()
                    val msg = res.body()?.message ?: errorBody ?: "Order failed"
                    
                    if (msg.contains("Insufficient stock", ignoreCase = true) || msg.contains("stock for item", true)) {
                        _uiState.value = CheckoutUiState.Error("⚠️ Stock Issue: $msg.\nPlease remove unavailable items.")
                    } else {
                        _uiState.value = CheckoutUiState.Error("Failed: $msg")
                    }
                }
            } catch (e: Exception) {
                _uiState.value = CheckoutUiState.Error("Network Error: ${e.message}")
            }
        }
    }

    fun setAddress(addressId: Int) {
        selectedAddressId.value = addressId
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
