package com.example.floatingflavors.app.feature.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.orders.data.OrdersRepository
import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrderDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val repository: OrdersRepository = OrdersRepository()
) : ViewModel() {

    private val _orders = MutableStateFlow<List<OrderDto>>(emptyList())
    val orders: StateFlow<List<OrderDto>> = _orders

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // For selected order detail
    private val _selectedOrder = MutableStateFlow<OrderDto?>(null)
    val selectedOrder: StateFlow<OrderDto?> = _selectedOrder

    // ---- Search + UI states ----
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab

    // Debounced query for UI filtering
    private val _debouncedQuery = MutableStateFlow("")
    private var debounceJob: Job? = null

    // Tab counts derived from _orders
    val tabCounts: StateFlow<Map<String, Int>> = orders
        .map { list ->
            val pending = list.count { (it.status ?: "").equals("pending", ignoreCase = true) }
            val active = list.count { (it.status ?: "").equals("active", ignoreCase = true) }
            val completed = list.count { (it.status ?: "").equals("completed", ignoreCase = true) || (it.status ?: "").equals("done", ignoreCase = true) }
            mapOf("all" to list.size, "pending" to pending, "active" to active, "completed" to completed)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, mapOf("all" to 0, "pending" to 0, "active" to 0, "completed" to 0))

    // Client-side filtered orders: tabs + debounced search
    val filteredOrders: StateFlow<List<OrderDto>> = combine(orders, _selectedTab, _debouncedQuery) { list, tab, query ->
        val q = query.trim().lowercase()
        val byTab = when (tab) {
            1 -> list.filter { (it.status ?: "").equals("pending", ignoreCase = true) }
            2 -> list.filter { (it.status ?: "").equals("active", ignoreCase = true) }
            3 -> list.filter { (it.status ?: "").equals("completed", ignoreCase = true) || (it.status ?: "").equals("done", ignoreCase = true) }
            else -> list
        }
        if (q.isBlank()) return@combine byTab

        byTab.filter { order ->
            val idMatch = (order.id ?: "").contains(q, ignoreCase = true)
            val nameMatch = (order.customer_name ?: "").lowercase().contains(q)
            val itemsMatch = order.items?.any { (it.name ?: "").lowercase().contains(q) } ?: false
            idMatch || nameMatch || itemsMatch
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ---- functions ----
    fun setSearchQuery(q: String) {
        _searchQuery.value = q
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(400)
            _debouncedQuery.value = q
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val resp = repository.getOrders()
                if (resp.success) {
                    _orders.value = resp.data ?: emptyList()
                } else {
                    _error.value = resp.message ?: "Failed to load orders"
                }
            } catch (e: Exception) {
                _error.value = "Failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // load single order detail from server
    fun loadOrderDetail(id: Int) {
        viewModelScope.launch {
            try {
                val resp = repository.getOrderDetail(id)
                if (resp.success && resp.data != null) {
                    _selectedOrder.value = resp.data
                } else {
                    _selectedOrder.value = null
                    _error.value = resp.message ?: "Failed to load order detail"
                }
            } catch (e: Exception) {
                _selectedOrder.value = null
                _error.value = "Failed: ${e.message}"
            }
        }
    }

    fun clearSelectedOrder() {
        _selectedOrder.value = null
    }

    // Update status optimistic (list + selectedOrder updated), rollback on failure
    fun updateOrderStatusOptimistic(orderId: String, newStatus: String, onResult: (Boolean, String?) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            // snapshot list and selected
            val currentList = _orders.value.toMutableList()
            val idx = currentList.indexOfFirst { it.id == orderId }
            val oldStatus = if (idx != -1) currentList[idx].status else null

            // optimistic list update
            if (idx != -1) {
                currentList[idx] = currentList[idx].copy(status = newStatus)
                _orders.value = currentList
            }

            // optimistic selected update
            val sel = _selectedOrder.value
            if (sel != null && sel.id == orderId) {
                _selectedOrder.value = sel.copy(status = newStatus)
            }

            try {
                val resp = repository.updateOrderStatus(orderId.toInt(), newStatus)
                if (resp.success) {
                    // update succeeded; optionally reload detail to get latest
                    onResult(true, null)
                } else {
                    // rollback
                    if (idx != -1) {
                        currentList[idx] = currentList[idx].copy(status = oldStatus)
                        _orders.value = currentList
                    }
                    if (sel != null && sel.id == orderId) {
                        _selectedOrder.value = sel.copy(status = oldStatus)
                    }
                    onResult(false, resp.message ?: "Failed to update status")
                }
            } catch (e: Exception) {
                // rollback
                if (idx != -1) {
                    currentList[idx] = currentList[idx].copy(status = oldStatus)
                    _orders.value = currentList
                }
                if (sel != null && sel.id == orderId) {
                    _selectedOrder.value = sel.copy(status = oldStatus)
                }
                onResult(false, e.message)
            }
        }
    }

    // helper used by dialog flows (keeps naming consistent)
    fun updateStatusFromDialog(orderId: String, newStatus: String, onComplete: (Boolean, String?) -> Unit) {
        updateOrderStatusOptimistic(orderId, newStatus, onComplete)
    }
}



//// file: OrdersViewModel.kt (same package com.example.floatingflavors.app.feature.orders.presentation)
//package com.example.floatingflavors.app.feature.orders.presentation
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.floatingflavors.app.feature.orders.data.OrdersRepository
//import com.example.floatingflavors.app.feature.orders.data.remote.dto.OrderDto
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.*
//import kotlinx.coroutines.launch
//
//class OrdersViewModel(
//    private val repository: OrdersRepository = OrdersRepository()
//) : ViewModel() {
//
//    private val _orders = MutableStateFlow<List<OrderDto>>(emptyList())
//    val orders: StateFlow<List<OrderDto>> = _orders
//
//    private val _isLoading = MutableStateFlow(false)
//    val isLoading: StateFlow<Boolean> = _isLoading
//
//    private val _error = MutableStateFlow<String?>(null)
//    val error: StateFlow<String?> = _error
//
//    // For selected order detail (already have)
//    private val _selectedOrder = MutableStateFlow<OrderDto?>(null)
//    val selectedOrder: StateFlow<OrderDto?> = _selectedOrder
//
//    // ---- Search + UI states ----
//    // raw search query (immediate)
//    private val _searchQuery = MutableStateFlow("")
//    val searchQuery: StateFlow<String> = _searchQuery
//
//    // chosen tab index (0..3)
//    private val _selectedTab = MutableStateFlow(0)
//    val selectedTab: StateFlow<Int> = _selectedTab
//
//    // Debounced query used for filtering (updated after user stops typing)
//    private val _debouncedQuery = MutableStateFlow("")
//    private var debounceJob: Job? = null
//
//    // Exposed derived state: counts for badges (All, Pending, Active, Completed)
//    val tabCounts: StateFlow<Map<String, Int>> = orders
//        .map { list ->
//            val pending = list.count { it.status.equals("pending", ignoreCase = true) }
//            val active = list.count { it.status.equals("active", ignoreCase = true) }
//            val completed = list.count { it.status.equals("completed", ignoreCase = true) || it.status.equals("done", ignoreCase = true) }
//            mapOf("all" to list.size, "pending" to pending, "active" to active, "completed" to completed)
//        }.stateIn(viewModelScope, SharingStarted.Eagerly, mapOf("all" to 0, "pending" to 0, "active" to 0, "completed" to 0))
//
//    // Exposed filteredOrders: applies tab filter + debounced search
//    val filteredOrders: StateFlow<List<OrderDto>> = combine(orders, _selectedTab, _debouncedQuery) { list, tab, query ->
//        val q = query.trim().lowercase()
//        // tab filtering
//        val byTab = when (tab) {
//            1 -> list.filter { (it.status ?: "").equals("pending", ignoreCase = true) }
//            2 -> list.filter { (it.status ?: "").equals("active", ignoreCase = true) }
//            3 -> list.filter { (it.status ?: "").equals("completed", ignoreCase = true) || (it.status ?: "").equals("done", ignoreCase = true) }
//            else -> list
//        }
//        if (q.isBlank()) return@combine byTab
//
//        // search fields: id, customer_name, item names
//        byTab.filter { order ->
//            val idMatch = (order.id ?: "").contains(q, ignoreCase = true)
//            val nameMatch = (order.customer_name ?: "").lowercase().contains(q)
//            val itemsMatch = order.items?.any { (it.name ?: "").lowercase().contains(q) } ?: false
//            idMatch || nameMatch || itemsMatch
//        }
//    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
//
//    // ---- functions ----
//    fun setSearchQuery(q: String) {
//        _searchQuery.value = q
//        // debounce: update _debouncedQuery after 400ms idle
//        debounceJob?.cancel()
//        debounceJob = viewModelScope.launch {
//            delay(400)
//            _debouncedQuery.value = q
//        }
//    }
//
//    fun setSelectedTab(tab: Int) {
//        _selectedTab.value = tab
//    }
//
//    fun loadOrders() {
//        viewModelScope.launch {
//            _isLoading.value = true
//            _error.value = null
//            try {
//                val resp = repository.getOrders()
//                if (resp.success) {
//                    _orders.value = resp.data ?: emptyList()
//                } else {
//                    _error.value = resp.message ?: "Failed to load orders"
//                }
//            } catch (e: Exception) {
//                _error.value = "Failed: ${e.message}"
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }
//
//    // existing functions you already have: loadOrderDetail, updateStatus..., etc.
//    // keep updateOrderStatusOptimistic, rollback logic, selectedOrder loading methods
//    // make sure updateOrderStatusOptimistic updates _orders and _selectedOrder as earlier provided
//}
