package com.example.floatingflavors.app.chatbot.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),

    // ✅ GROUP SUPPORT (default = single chat)
    val senderName: String? = null,
    val groupId: String? = null
)
