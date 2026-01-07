package com.example.floatingflavors.app.chatbot.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.chatbot.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isTyping: Boolean = false
)

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private var welcomeSent = false   // 🔐 important

    /** 🔥 AUTO WELCOME */
    fun sendWelcomeIfNeeded(userId: Int) {
        if (welcomeSent || _messages.value.isNotEmpty()) return

        welcomeSent = true

        // typing indicator
        _messages.value = _messages.value + ChatMessage("Typing...", false, true)

        viewModelScope.launch {
            try {
                val reply = repository.sendMessage(userId, "__WELCOME__")
                _messages.value =
                    _messages.value.dropLast(1) + ChatMessage(reply, false)
            } catch (e: Exception) {
                _messages.value =
                    _messages.value.dropLast(1) +
                            ChatMessage("Welcome to Floating Flavors 👋", false)
            }
        }
    }

    /** NORMAL USER MESSAGE */
    fun sendMessage(userId: Int, text: String) {
        _messages.value = _messages.value + ChatMessage(text, true)
        _messages.value = _messages.value + ChatMessage("Typing...", false, true)

        viewModelScope.launch {
            try {
                val reply = repository.sendMessage(userId, text)
                _messages.value =
                    _messages.value.dropLast(1) + ChatMessage(reply, false)
            } catch (e: Exception) {
                _messages.value =
                    _messages.value.dropLast(1) +
                            ChatMessage("Something went wrong 😢", false)
            }
        }
    }
}
