package com.example.floatingflavors.app.chatbot

import com.example.floatingflavors.app.chatbot.data.ChatBotResponse
import com.example.floatingflavors.app.chatbot.data.ChatDao
import com.example.floatingflavors.app.chatbot.data.ChatEntity
import com.example.floatingflavors.app.chatbot.data.ChatRequest

import com.google.gson.Gson

class ChatRepository(
    private val api: ChatApi,
    private val dao: ChatDao
) {
    private val gson = Gson()

    suspend fun sendMessage(
        userId: Int,
        message: String
    ): Result<ChatBotResponse> = runCatching {
        // Save user message to local DB
        dao.insert(ChatEntity(text = message, isUser = true))

        val response = api.sendMessage(
            ChatRequest(user_id = userId, message = message)
        )

        // Save bot response with rich details
        val botText = response.reply ?: response.message ?: response.cta ?: "Here are the results:"
        val metadataMap = mutableMapOf<String, Any>()
        response.menu_items?.let { metadataMap["menu_items"] = it }
        response.orders?.let { metadataMap["orders"] = it }
        response.events?.let { metadataMap["events"] = it }
        response.corporate_bookings?.let { metadataMap["corporate_bookings"] = it }
        response.quick_replies?.let { metadataMap["quick_replies"] = it }
        response.cta?.let { metadataMap["cta"] = it }
        response.title?.let { metadataMap["title"] = it }

        val jsonStr = if (metadataMap.isNotEmpty()) gson.toJson(metadataMap) else null

        dao.insert(
            ChatEntity(
                text = botText,
                isUser = false,
                type = response.type,
                jsonMetadata = jsonStr,
                confidence = response.confidence
            )
        )

        response
    }

    suspend fun getMessages(): List<ChatEntity> {
        val list = dao.getAll()
        if (list.isEmpty()) {
            val welcome = ChatEntity(
                text = "Welcome to Floating Flavors AI! 🤖 How can I help you today? Ask me about menu items, orders, or catering bookings.",
                isUser = false
            )
            dao.insert(welcome)
            return listOf(welcome)
        }
        return list
    }

    suspend fun clearMessages() {
        dao.clear()
    }
}

