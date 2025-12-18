package com.example.floatingflavors.app.feature.user.data.settings

import com.example.floatingflavors.app.feature.user.data.settings.dto.ToggleNotificationRequest
import com.example.floatingflavors.app.feature.user.data.settings.dto.UpdateLanguageRequest
import com.example.floatingflavors.app.feature.user.data.settings.dto.UserSettingsDto

class UserSettingsRepository(
    private val api: UserSettingsApi
) {

    suspend fun fetchSettings(userId: Int): UserSettingsDto {
        val response = api.getUserSettings(userId)

        if (response.success && response.data != null) {
            return response.data
        } else {
            throw Exception("Failed to load settings")
        }
    }


    suspend fun updateLanguage(userId: Int, language: String) =
        api.updateLanguage(UpdateLanguageRequest(userId, language))

    suspend fun toggleNotification(userId: Int, enabled: Boolean) =
        api.toggleNotification(ToggleNotificationRequest(userId, enabled))

    suspend fun logout(userId: Int) =
        api.logout(mapOf("user_id" to userId))

    suspend fun deleteAccount(userId: Int) =
        api.deleteAccount(mapOf("user_id" to userId))
}
