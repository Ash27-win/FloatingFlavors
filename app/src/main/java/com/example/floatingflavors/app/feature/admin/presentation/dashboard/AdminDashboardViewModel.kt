package com.example.floatingflavors.app.feature.admin.presentation.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.core.data.local.AppDatabase
import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.notification.data.NotificationRepository
import com.example.floatingflavors.app.feature.order.data.remote.dto.OrdersCounts
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminDashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationRepository = NotificationRepository(
        com.example.floatingflavors.app.core.network.NetworkClient.notificationApi,
        AppDatabase.getDatabase(application)
    )

    // ✅ Unread Count for Badge
    val unreadCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _counts = MutableStateFlow<OrdersCounts?>(null)
    val counts: StateFlow<OrdersCounts?> = _counts

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _analytics = MutableStateFlow<com.example.floatingflavors.app.feature.admin.data.remote.AnalyticsDataDto?>(null)
    val analytics: StateFlow<com.example.floatingflavors.app.feature.admin.data.remote.AnalyticsDataDto?> = _analytics

    fun loadCounts() {
        viewModelScope.launch {
            // Also refresh notifications when dashboard loads
            launch { notificationRepository.refreshNotifications() }
            
            _isLoading.value = true
            try {
                // 1. Fetch Order Counts (Breakdown)
                val responseCounts = NetworkClient.ordersApi.getOrdersCounts()
                if (responseCounts.success && responseCounts.data != null) {
                    _counts.value = responseCounts.data
                }

                // 2. Fetch Analytics (Revenue, etc.)
                // Use runCatching to ensure one failure doesn't block the other
                runCatching {
                    val responseAnalytics = NetworkClient.adminApi.getAnalytics()
                    if (responseAnalytics.success) {
                        _analytics.value = responseAnalytics.data
                    }
                }.onFailure { it.printStackTrace() }

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
