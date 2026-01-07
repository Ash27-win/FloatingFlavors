package com.example.floatingflavors.app.chatbot

import com.example.floatingflavors.app.chatbot.data.ChatDao
import com.example.floatingflavors.app.chatbot.data.ChatEntity
import com.example.floatingflavors.app.chatbot.data.ChatRequest

class ChatRepository(
    private val api: ChatApi,
    private val dao: ChatDao
) {

    suspend fun sendMessage(userId: Int, text: String): String {

        // Save user message
        dao.insert(
            ChatEntity(
                text = text,
                isUser = true
            )
        )

        // API call
        val response = api.sendMessage(
            ChatRequest(
                user_id = userId,
                message = text
            )
        )

        // Save bot reply
        dao.insert(
            ChatEntity(
                text = response.reply,
                isUser = false
            )
        )

        return response.reply
    }

    suspend fun getHistory(): List<ChatEntity> = dao.getAll()
}
