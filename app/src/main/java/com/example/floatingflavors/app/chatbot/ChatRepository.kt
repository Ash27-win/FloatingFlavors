package com.example.floatingflavors.app.chatbot

import com.example.floatingflavors.app.chatbot.data.ChatBotResponse
import com.example.floatingflavors.app.chatbot.data.ChatDao
import com.example.floatingflavors.app.chatbot.data.ChatEntity
import com.example.floatingflavors.app.chatbot.data.ChatRequest

class ChatRepository(
    private val api: ChatApi,
    private val dao: ChatDao
) {

    suspend fun sendMessage(
        userId: Int,
        message: String
    ): Result<ChatBotResponse> = runCatching {
        // Save user message to local DB
        dao.insert(ChatEntity(text = message, isUser = true))

        val response = api.sendMessage(
            ChatRequest(user_id = userId, message = message)
        )

        // Save bot text ONLY if reply or message exists
        val botText = response.reply ?: response.message
        botText?.let {
            dao.insert(ChatEntity(text = it, isUser = false))
        }

        response
    }

    suspend fun getMessages() = dao.getAll()
}

