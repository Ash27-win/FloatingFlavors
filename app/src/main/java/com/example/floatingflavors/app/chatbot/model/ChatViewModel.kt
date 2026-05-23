package com.example.floatingflavors.app.chatbot.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.chatbot.ChatRepository
import com.example.floatingflavors.app.chatbot.data.ChatEntity
import com.example.floatingflavors.app.chatbot.data.CorporateDto
import com.example.floatingflavors.app.chatbot.data.EventDto
import com.example.floatingflavors.app.chatbot.data.OrderDto
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    // 🔹 Chat text messages (Room)
    private val _messages = MutableStateFlow<List<ChatEntity>>(emptyList())
    val messages: StateFlow<List<ChatEntity>> = _messages

    // 🔹 Menu results
    private val _menuItems = MutableStateFlow<List<MenuItemDto>>(emptyList())
    val menuItems: StateFlow<List<MenuItemDto>> = _menuItems

    // 🔹 Order history
    private val _orders = MutableStateFlow<List<OrderDto>>(emptyList())
    val orders: StateFlow<List<OrderDto>> = _orders

    // 🔹 Event history
    private val _events = MutableStateFlow<List<EventDto>>(emptyList())
    val events: StateFlow<List<EventDto>> = _events

    // 🔹 Corporate history
    private val _corporates = MutableStateFlow<List<CorporateDto>>(emptyList())
    val corporates: StateFlow<List<CorporateDto>> = _corporates

    // 🔹 UI STATE
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var lastUserId: Int? = null
    private var lastMessage: String? = null

    fun loadMessages() {
        viewModelScope.launch {
            _messages.value = repository.getMessages()
        }
    }

    fun sendMessage(userId: Int, text: String) {
        lastUserId = userId
        lastMessage = text
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            val result = repository.sendMessage(userId, text)
            
            result.onSuccess { response ->
                _isLoading.value = false
                
                // 🔹 CLEAR OLD STRUCTURED DATA
                _menuItems.value = emptyList()
                _orders.value = emptyList()
                _events.value = emptyList()
                _corporates.value = emptyList()

                when (response.type) {
                    "menu_result", "food_carousel" -> {
                        _menuItems.value = response.menu_items ?: emptyList()
                    }
                    "order_history", "order_tracking" -> {
                        _orders.value = response.orders ?: emptyList()
                    }
                    "event_history" -> {
                        _events.value = response.events ?: emptyList()
                    }
                    "corporate_history" -> {
                        _corporates.value = response.corporate_bookings ?: emptyList()
                    }
                }
                loadMessages()
            }.onFailure { exception ->
                _isLoading.value = false
                _error.value = when (exception) {
                    is java.net.SocketTimeoutException -> "Server timed out. Please try again."
                    is java.net.UnknownHostException -> "No internet connection."
                    else -> "Something went wrong: ${exception.message}"
                }
            }
        }
    }

    private val cartRepository = com.example.floatingflavors.app.feature.user.data.cart.CartRepository()

    fun addToCart(userId: Int, menuItemId: Int, price: Int) {
        viewModelScope.launch {
            try {
                cartRepository.addItem(userId, menuItemId, price)
                loadMessages()
            } catch (e: Exception) {
                // log or ignore
            }
        }
    }

    fun retry() {
        val uid = lastUserId
        val msg = lastMessage
        if (uid != null && msg != null) {
            sendMessage(uid, msg)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearMessages()
            loadMessages()
        }
    }
}
