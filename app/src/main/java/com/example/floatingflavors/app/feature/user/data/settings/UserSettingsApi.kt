package com.example.floatingflavors.app.feature.user.data.settings

import com.example.floatingflavors.app.feature.user.data.settings.dto.ToggleNotificationRequest
import com.example.floatingflavors.app.feature.user.data.settings.dto.UpdateLanguageRequest
import com.example.floatingflavors.app.feature.user.data.settings.dto.UpdateProfileRequest
import com.example.floatingflavors.app.feature.user.data.settings.dto.UserSettingsDto
import retrofit2.Call
import retrofit2.http.*

data class SettingsResponse<T>(
    val success: Boolean,
    val data: T?,
    val app: AppInfo?
)

data class AppInfo(
    val version: String,
    val build: String
)

interface UserSettingsApi {

    @GET("user_settings_get.php")
    suspend fun getUserSettings(
        @Query("user_id") userId: Int
    ): SettingsResponse<UserSettingsDto>

    @POST("settings_update_profile.php")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Map<String, Any>

    @POST("settings_update_language.php")
    suspend fun updateLanguage(
        @Body request: UpdateLanguageRequest
    ): Map<String, Any>

    @POST("settings_toggle_notification.php")
    suspend fun toggleNotification(
        @Body request: ToggleNotificationRequest
    ): Map<String, Any>

    @POST("user_logout.php")
    suspend fun logout(
        @Body body: Map<String, Int>
    ): Map<String, Any>

    @POST("user_delete_account.php")
    suspend fun deleteAccount(
        @Body body: Map<String, Int>
    ): Map<String, Any>
}

