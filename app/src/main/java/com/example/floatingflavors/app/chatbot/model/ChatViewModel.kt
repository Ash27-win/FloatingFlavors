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

    fun loadMessages() {
        viewModelScope.launch {
            _messages.value = repository.getMessages()
        }
    }

    fun sendMessage(userId: Int, text: String) {
        viewModelScope.launch {
            val response = repository.sendMessage(userId, text)

            // 🔹 CLEAR OLD STRUCTURED DATA
            _menuItems.value = emptyList()
            _orders.value = emptyList()
            _events.value = emptyList()
            _corporates.value = emptyList()

            when (response.type) {
                "menu_result" -> {
                    _menuItems.value = response.menu_items ?: emptyList()
                }
                "order_history" -> {
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
        }
    }
}
