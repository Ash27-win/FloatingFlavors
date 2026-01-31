package com.example.floatingflavors.app.feature.notification.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val body: String,
    val timestamp: Long,
    val isRead: Boolean = false,
    val role: String, // "USER", "ADMIN", "DELIVERY"
    val type: String? = null, // "NEW_ORDER", "OFFER", etc.
    val referenceId: String? = null // Order ID etc.
)
