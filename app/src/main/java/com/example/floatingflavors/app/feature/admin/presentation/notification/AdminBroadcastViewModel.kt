package com.example.floatingflavors.app.feature.admin.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.notification.data.NotificationRepository
import com.example.floatingflavors.app.feature.notification.data.remote.SimpleResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminBroadcastViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow<BroadcastState>(BroadcastState.Idle)
    val state: StateFlow<BroadcastState> = _state

    fun sendBroadcast(title: String, body: String, role: String) {
        if (title.isBlank() || body.isBlank()) {
            _state.value = BroadcastState.Error("Title and message cannot be empty")
            return
        }

        viewModelScope.launch {
            _state.value = BroadcastState.Loading
            try {
                val response = repository.broadcast(title, body, role)
                if (response.success) {
                    _state.value = BroadcastState.Success(response.message)
                } else {
                    _state.value = BroadcastState.Error(response.message)
                }
            } catch (e: Exception) {
                _state.value = BroadcastState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _state.value = BroadcastState.Idle
    }
}

sealed class BroadcastState {
    object Idle : BroadcastState()
    object Loading : BroadcastState()
    data class Success(val message: String) : BroadcastState()
    data class Error(val message: String) : BroadcastState()
}
