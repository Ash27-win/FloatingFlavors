package com.example.floatingflavors.app.feature.user.data.settings.dto

data class ToggleNotificationRequest(
    val user_id: Int,
    val enabled: Boolean
)
