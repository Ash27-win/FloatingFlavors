package com.example.floatingflavors.app.feature.admin.presentation.notification

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

class AdminNotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = com.example.floatingflavors.app.feature.notification.data.NotificationRepository(
        com.example.floatingflavors.app.core.network.NetworkClient.notificationApi,
        AppDatabase.getDatabase(application)
    )

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            repository.refreshNotifications()
        }
    }

    // Filter for ADMIN Role - show all for now as requested or filter? User said "Admin Notification Screen". 
    // Usually Admin wants to see ADMIN alerts.
    // Ideally we should filter by role = 'ADMIN' OR 'BROADCAST'.
    // But since the API returns notifications for the logged in user based on the token role,
    // we can just show everything in the DB that matches. 
    // Since this is the Admin App, the user is logged in as Admin.
    
    val notifications: StateFlow<List<NotificationUiModel>> = repository.dao.getAllNotifications()
        .map { entities -> 
            entities
                .sortedByDescending { it.timestamp }
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
