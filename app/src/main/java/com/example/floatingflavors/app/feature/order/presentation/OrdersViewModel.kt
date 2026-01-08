package com.example.floatingflavors.app.feature.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingDto
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

    private val bookingMap = mutableMapOf<String, AdminBookingDto>()

    // For selected booking
    private val _selectedBooking = MutableStateFlow<AdminBookingDto?>(null)
    val selectedBooking: StateFlow<AdminBookingDto?> = _selectedBooking

    // ---------------- TAB COUNTS (ONLY LOGIC ADDED) ----------------
    val tabCounts: StateFlow<Map<String, Int>> = orders
        .map { list ->
            val pending = list.count {
                (it.status ?: "").equals("pending", ignoreCase = true)
            }

            // ✅ FIX: active includes OUT_FOR_DELIVERY
            val active = list.count {
                (it.status ?: "").equals("active", ignoreCase = true) ||
                        (it.status ?: "").equals("out_for_delivery", ignoreCase = true)
            }

            val completed = list.count {
                (it.status ?: "").equals("completed", ignoreCase = true) ||
                        (it.status ?: "").equals("done", ignoreCase = true)
            }

            mapOf(
                "all" to list.size,
                "pending" to pending,
                "active" to active,
                "completed" to completed
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            mapOf("all" to 0, "pending" to 0, "active" to 0, "completed" to 0)
        )

    // ---------------- FILTERED ORDERS (ONLY LOGIC ADDED) ----------------
    val filteredOrders: StateFlow<List<OrderDto>> =
        combine(orders, _selectedTab, _debouncedQuery) { list, tab, query ->
            val q = query.trim().lowercase()

            val byTab = when (tab) {
                1 -> list.filter {
                    (it.status ?: "").equals("pending", ignoreCase = true)
                }

                // ✅ FIX: active tab includes OUT_FOR_DELIVERY
                2 -> list.filter {
                    (it.status ?: "").equals("active", ignoreCase = true) ||
                            (it.status ?: "").equals("out_for_delivery", ignoreCase = true)
                }

                3 -> list.filter {
                    (it.status ?: "").equals("completed", ignoreCase = true) ||
                            (it.status ?: "").equals("done", ignoreCase = true)
                }

                else -> list
            }

            if (q.isBlank()) return@combine byTab

            byTab.filter { order ->
                val idMatch = (order.id ?: "").contains(q, ignoreCase = true)
                val nameMatch = (order.customer_name ?: "").lowercase().contains(q)
                val itemsMatch =
                    order.items?.any { (it.name ?: "").lowercase().contains(q) } ?: false

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

    fun selectBooking(order: OrderDto) {
        val realId = order.bookingId ?: return
        _selectedBooking.value = bookingMap[realId]
    }

    // 🔥 CORE LOGIC
    fun loadOrders() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val foodOrdersResponse = repository.getOrders()
                val foodOrders = if (foodOrdersResponse.success) {
                    foodOrdersResponse.data ?: emptyList()
                } else emptyList()

                val bookings: List<AdminBookingDto> = repository.getEventBookings()

                val bookingOrders: List<OrderDto> = bookings.map { booking ->
                    val bookingIdStr = booking.id?.toString() ?: "0"
                    bookingMap[bookingIdStr] = booking

                    OrderDto(
                        id = "B-$bookingIdStr",
                        customer_name =
                            if (booking.booking_type == "EVENT")
                                booking.event_name ?: "Event Booking"
                            else
                                booking.company_name ?: "Company Contract",
                        status = when (booking.status) {
                            "PENDING" -> "pending"
                            "CONFIRMED" -> "active"
                            "CANCELLED" -> "rejected"
                            else -> "pending"
                        },
                        created_at = booking.created_at,
                        time_ago = null,
                        items = emptyList(),
                        amount = null,
                        distance = null,
                        delivery_partner_id = null,
                        isBooking = true,
                        bookingId = bookingIdStr
                    )
                }

                _orders.value = foodOrders + bookingOrders

            } catch (e: Exception) {
                _error.value = e.message
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
                if (resp.status && resp.data != null) {
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
    fun updateOrderStatusOptimistic(
        orderId: String,
        newStatus: String,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val currentList = _orders.value.toMutableList()
            val idx = currentList.indexOfFirst { it.id == orderId }
            val oldStatus = if (idx != -1) currentList[idx].status else null

            if (idx != -1) {
                currentList[idx] = currentList[idx].copy(
                    status = newStatus,
                    delivery_partner_id = currentList[idx].delivery_partner_id
                )
                _orders.value = currentList
            }

            val sel = _selectedOrder.value
            if (sel != null && sel.id == orderId) {
                _selectedOrder.value = sel.copy(
                    status = newStatus,
                    delivery_partner_id = sel.delivery_partner_id
                )
            }

            try {
                val resp = repository.updateOrderStatus(orderId, newStatus)
                if (resp.status) {
                    onResult(true, null)
                } else {
                    rollback(idx, oldStatus, currentList, sel)
                    onResult(false, resp.message)
                }
            } catch (e: Exception) {
                rollback(idx, oldStatus, currentList, sel)
                onResult(false, e.message)
            }
        }
    }

    private fun rollback(
        idx: Int,
        oldStatus: String?,
        list: MutableList<OrderDto>,
        sel: OrderDto?
    ) {
        if (idx != -1) {
            list[idx] = list[idx].copy(status = oldStatus)
            _orders.value = list
        }
        if (sel != null && sel.id == list.getOrNull(idx)?.id) {
            _selectedOrder.value = sel.copy(status = oldStatus)
        }
    }

    // helper used by dialog flows
    fun updateStatusFromDialog(
        orderId: String,
        newStatus: String,
        deliveryPartnerId: Int? = null,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.updateOrderStatus(
                    orderId = orderId,
                    newStatus = newStatus,
                    deliveryPartnerId = deliveryPartnerId
                )

                if (result.status) {
                    _orders.value = _orders.value.map { order ->
                        if (order.id == orderId) {
                            order.copy(
                                status = newStatus,
                                delivery_partner_id = deliveryPartnerId?.toString()
                            )
                        } else order
                    }
                    callback(true, null)
                } else callback(false, result.message)

            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
    }

    fun clearBooking() {
        _selectedBooking.value = null
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            try {
                repository.updateBookingStatus(bookingId, status)
                clearBooking()
                loadOrders()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ---------------- REJECT ORDER WITH REASON (REQUIRED BY ADMIN UI) ----------------
    fun rejectOrderWithReason(
        orderId: String,
        reason: String,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val result = repository.rejectOrder(orderId, reason)

                if (result.status) {
                    // update list immediately
                    _orders.value = _orders.value.map { order ->
                        if (order.id == orderId) {
                            order.copy(status = "rejected")
                        } else order
                    }

                    // update selected order if dialog is open
                    _selectedOrder.value?.let { selected ->
                        if (selected.id == orderId) {
                            _selectedOrder.value = selected.copy(status = "rejected")
                        }
                    }

                    callback(true, result.message)
                } else {
                    callback(false, result.message)
                }

            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
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
