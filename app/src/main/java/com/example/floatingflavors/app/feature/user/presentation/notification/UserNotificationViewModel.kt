package com.example.floatingflavors.app.feature.user.presentation.notification

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.core.data.local.AppDatabase
import com.example.floatingflavors.app.feature.delivery.presentation.notification.NotificationUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserNotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = com.example.floatingflavors.app.feature.notification.data.NotificationRepository(
        com.example.floatingflavors.app.core.network.NetworkClient.notificationApi,
        AppDatabase.getDatabase(application)
    )

    init {
        viewModelScope.launch {
            repository.refreshNotifications()
        }
    }

    // Filter for USER Role
    val notifications: StateFlow<List<NotificationUiModel>> = repository.dao.getAllNotifications()
        .map { entities -> 
            entities.filter { it.role == "USER" || it.role == "UNKNOWN" } // Show looser matches
                .map { entity ->
                NotificationUiModel(
                    id = entity.id,
                    title = entity.title,
                    message = entity.body,
                    time = formatTime(entity.timestamp),
                    isRead = entity.isRead,
                    type = entity.type
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markRead(id)
        }
    }
    
    fun delete(id: Long) {
        viewModelScope.launch {
            repository.dao.deleteNotification(id)
        }
    }

    private fun formatTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / (1000 * 60)
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "$minutes mins ago"
            hours < 24 -> "$hours hours ago"
            else -> "$days days ago"
        }
    }
}
