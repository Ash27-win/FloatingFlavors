package com.example.floatingflavors.app.chatbot

import com.example.floatingflavors.app.chatbot.data.ChatBotResponse
import com.example.floatingflavors.app.chatbot.data.ChatRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatApi {

    @POST("chat")
    suspend fun sendMessage(
        @Body request: ChatRequest
    ): ChatBotResponse
}
