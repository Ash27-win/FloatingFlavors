package com.example.floatingflavors.app.feature.notification.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface NotificationApi {

    @GET("get_notifications.php")
    suspend fun getNotifications(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): NotificationResponse

    @POST("mark_notification_read.php")
    suspend fun markRead(
        @Body body: MarkReadRequest
    ): SimpleResponse

    @POST("admin_broadcast.php")
    suspend fun sendBroadcast(
        @Body body: BroadcastRequest
    ): SimpleResponse
}

data class NotificationResponse(
    val success: Boolean,
    val data: List<NotificationDto>,
    @SerializedName("unread_count") val unreadCount: Int
)

data class NotificationDto(
    val id: Long,
    val title: String,
    val body: String,
    val type: String?,
    @SerializedName("reference_id") val referenceId: String?,
    @SerializedName("is_read") val isRead: Int, // 0 or 1
    @SerializedName("created_at") val createdAt: String
)

data class MarkReadRequest(
    @SerializedName("notification_id") val notificationId: Long? = null 
)

data class BroadcastRequest(
    val title: String,
    val body: String,
    @SerializedName("target_role") val targetRole: String
)

data class SimpleResponse(
    val success: Boolean,
    val message: String
)
