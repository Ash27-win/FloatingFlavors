package com.example.floatingflavors.app.core.service

import android.util.Log
import com.example.floatingflavors.app.feature.auth.data.AuthRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asSharedFlow

// ... imports
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.floatingflavors.MainActivity
import com.example.floatingflavors.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")

        // Sync token with backend
        val repo = AuthRepository()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repo.updateFcmToken(token)
                Log.d("FCM", "Token synced with backend")
            } catch (e: Exception) {
                Log.e("FCM", "Failed to sync token", e)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "From: ${remoteMessage.from}")

        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            
            val type = remoteMessage.data["type"]
            val orderId = remoteMessage.data["order_id"]
            val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Floating Flavors"
            val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "New update available"
            val role = remoteMessage.data["role"] ?: "USER" // Default to User if not specified

            // 1. Persist to Local DB
            val dao = com.example.floatingflavors.app.core.data.local.AppDatabase.getDatabase(applicationContext).notificationDao()
            val entity = com.example.floatingflavors.app.feature.notification.data.local.NotificationEntity(
                title = title,
                body = body,
                timestamp = System.currentTimeMillis(),
                isRead = false,
                role = role,
                type = type,
                referenceId = orderId ?: remoteMessage.data["reference_id"]
            )
            
            CoroutineScope(Dispatchers.IO).launch {
                dao.insertNotification(entity)
            }
            
            // 2. Emit Event for Foreground UI Update (Auto-Refresh)
            if (type == "NEW_ORDER" && orderId != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    NotificationEventBus.emitEvent(NotificationEvent.NewOrder(orderId))
                }
            }
            
            // 3. 🔥 SHOW HEADS-UP NOTIFICATION (Foreground & Background Data)
            // Inject inferred screen if missing
            val mutableData = remoteMessage.data.toMutableMap()
            if (!mutableData.containsKey("screen")) {
                if (type == "NEW_ORDER") {
                    mutableData["screen"] = "OrderTrackingScreen"
                } else if (type == "ORDER_UPDATE") {
                    mutableData["screen"] = "UserOrderDetails"
                    // Ensure reference_id is set
                    if (!mutableData.containsKey("reference_id") && orderId != null) {
                        mutableData["reference_id"] = orderId
                    }
                }
            }
            
            showNotification(title, body, mutableData)
        } else {
             // Notification only payload
             remoteMessage.notification?.let {
                 Log.d("FCM", "Message Notification Body: ${it.body}")
                 val title = it.title ?: "Floating Flavors"
                 val bodyStr = it.body ?: ""
                 
                 // Persist as well
                 val dao = com.example.floatingflavors.app.core.data.local.AppDatabase.getDatabase(applicationContext).notificationDao()
                 val entity = com.example.floatingflavors.app.feature.notification.data.local.NotificationEntity(
                    title = title,
                    body = bodyStr,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    role = "USER", // Default
                    type = "GENERAL",
                    referenceId = null
                )
                CoroutineScope(Dispatchers.IO).launch {
                    dao.insertNotification(entity)
                }

                 showNotification(title, bodyStr, emptyMap())
             }
        }
    }

    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val channelId = "floating_flavors_channel"
        
        // Deep Link Intent
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP) // Clear top is okay if SingleTop is there, but usually SingleTop is enough. Actually, standard is SINGLE_TOP.
            // If we use CLEAR_TOP, it destroys the activity. 
            // We want to KEEP the stack.
            // But wait, if we aredeep in stack, we might want to clear top? The user said "na already ulla tha irukan... just navigate".
            // So we do NOT want to clear top.
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // Pass all data keys to intent
            data.forEach { (k, v) -> putExtra(k, v) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Ensure this resource exists
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Heads-Up
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // Sound + Vibrate
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create Channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH // Heads-Up Config
            ).apply {
                description = "Notifications for order status and delivery updates"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

// 🔥 SIMPLE EVENT BUS
object NotificationEventBus {
    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<NotificationEvent>(replay = 1)
    val events = _events.asSharedFlow()

    suspend fun emitEvent(event: NotificationEvent) {
        _events.emit(event)
    }
}

sealed class NotificationEvent {
    data class NewOrder(val orderId: String) : NotificationEvent()
    data class Navigate(val screen: String, val referenceId: String?) : NotificationEvent()
}
