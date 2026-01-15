package com.example.floatingflavors.app.chatbot.components

import java.text.SimpleDateFormat
import java.util.*

object TimeUtils {
    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
