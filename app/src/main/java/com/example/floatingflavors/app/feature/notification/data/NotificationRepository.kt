package com.example.floatingflavors.app.feature.notification.data

import com.example.floatingflavors.app.core.data.local.AppDatabase
import com.example.floatingflavors.app.feature.notification.data.local.NotificationEntity
import com.example.floatingflavors.app.feature.notification.data.remote.BroadcastRequest
import com.example.floatingflavors.app.feature.notification.data.remote.MarkReadRequest
import com.example.floatingflavors.app.feature.notification.data.remote.NotificationApi
import com.example.floatingflavors.app.feature.notification.data.remote.SimpleResponse
import kotlinx.coroutines.flow.Flow

class NotificationRepository(
    private val api: NotificationApi,
    private val db: AppDatabase
) {

    val dao = db.notificationDao()
    
    val unreadCount: Flow<Int> = dao.getUnreadCount()

    // 1. Fetch from Backend and Sync Local
    suspend fun refreshNotifications() {
        try {
            val response = api.getNotifications(limit = 50, offset = 0) // Basic sync
            if (response.success) {
                val entities = response.data.map { dto ->
                    NotificationEntity(
                        id = dto.id, // Ensure Entity ID is not auto-generated if we want to match backend ID, or handle conflict
                        title = dto.title,
                        body = dto.body,
                        timestamp = parseTime(dto.createdAt),
                        isRead = dto.isRead == 1,
                        role = "UNKNOWN", // API doesn't return role usually, we assume current user context
                        type = dto.type ?: "GENERAL",
                        referenceId = dto.referenceId
                    )
                }
                // Insert/Update Local
                // 🔥 Clear old data first to prevent multi-user mix (Server is Source of Truth)
                dao.deleteAll()
                dao.insertAll(entities)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Silent fail, show local data
        }
    }

    // 2. Mark Read (Both Backend and Local)
    suspend fun markRead(id: Long) {
        try {
            // Local
            dao.markAsRead(id)
            // Remote
            api.markRead(MarkReadRequest(id))
        } catch (e: Exception) {
           e.printStackTrace()
        }
    }
    
    // 3. Admin Broadcast
    suspend fun broadcast(title: String, body: String, role: String): SimpleResponse {
        return api.sendBroadcast(BroadcastRequest(title, body, role))
    }

    private fun parseTime(timeStr: String): Long {
         // Simple parser or just use current time if format complex
         // Backend sends 'created_at'. Let's trust local time for now or parse it.
         return System.currentTimeMillis() 
    }
}
