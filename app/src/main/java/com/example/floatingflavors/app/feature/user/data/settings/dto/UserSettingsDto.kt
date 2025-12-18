package com.example.floatingflavors.app.feature.user.data.settings.dto

data class UserSettingsDto(
    val id: String? = "",
    val name: String? = "",
    val email: String? = "",
    val language: String? = "",
    val notifications_enabled: String? = "0"
) {
    val isNotificationEnabled: Boolean
        get() = notifications_enabled == "1"
}


