package com.example.floatingflavors.app.feature.delivery.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.delivery.data.DeliveryRepository
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryNotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DeliveryNotificationUiState {
    object Loading : DeliveryNotificationUiState()
    data class Success(
        val notifications: List<DeliveryNotificationDto>,
        val unreadCount: Int
    ) : DeliveryNotificationUiState()
    data class Error(val message: String) : DeliveryNotificationUiState()
}

class DeliveryNotificationViewModel(
    private val repository: DeliveryRepository = DeliveryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DeliveryNotificationUiState>(DeliveryNotificationUiState.Loading)
    val uiState: StateFlow<DeliveryNotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = DeliveryNotificationUiState.Loading
            val response = repository.getNotifications(limit = 50, offset = 0)
            if (response.success) {
                _uiState.value = DeliveryNotificationUiState.Success(
                    notifications = response.notifications ?: emptyList(),
                    unreadCount = response.unreadCount
                )
            } else {
                _uiState.value = DeliveryNotificationUiState.Error(response.message ?: "Failed to fetch notifications.")
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            // Optimistic UI update
            val currentState = _uiState.value
            if (currentState is DeliveryNotificationUiState.Success) {
                val updatedList = currentState.notifications.map { 
                    if (it.id == notificationId) it.copy(isRead = 1) else it 
                }
                val newUnread = maxOf(0, currentState.unreadCount - 1)
                _uiState.value = currentState.copy(notifications = updatedList, unreadCount = newUnread)
            }

            val res = repository.markNotificationRead(notificationId)
            if (!res.success) {
                // Silently reload if failed to stay in sync
                loadNotifications()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val res = repository.markNotificationRead(null)
            if (res.success) {
                loadNotifications()
            }
        }
    }

    fun deleteNotification(notificationId: Int) {
        viewModelScope.launch {
            // Optimistic UI update
            val currentState = _uiState.value
            if (currentState is DeliveryNotificationUiState.Success) {
                val deletedNotif = currentState.notifications.find { it.id == notificationId }
                val wasUnread = deletedNotif?.isRead == 0
                val updatedList = currentState.notifications.filter { it.id != notificationId }
                val newUnread = if (wasUnread) maxOf(0, currentState.unreadCount - 1) else currentState.unreadCount
                
                _uiState.value = currentState.copy(notifications = updatedList, unreadCount = newUnread)
            }

            val res = repository.deleteNotification(notificationId)
            if (!res.success) {
                // If deletion fails, revert UI by reloading
                loadNotifications()
            }
        }
    }
}
