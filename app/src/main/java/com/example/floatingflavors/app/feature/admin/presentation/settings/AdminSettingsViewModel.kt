package com.example.floatingflavors.app.feature.admin.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.admin.data.remote.AdminSettingsRepository
import com.example.floatingflavors.app.feature.admin.data.remote.dto.AdminSettingsDto
import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminPrefRequest
import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminSettingsRequest
import com.example.floatingflavors.app.core.network.NetworkClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class AdminSettingsViewModel(private val repo: AdminSettingsRepository) : ViewModel() {

    private val _state = MutableStateFlow<AdminSettingsDto?>(null)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(replay = 0)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private val baseUrl: String = NetworkClient.BASE_URL

    /** Load settings */
    fun load(adminId: Int) {
        viewModelScope.launch {
            try {
                val resp = repo.getSettings(adminId)
                if (resp.success && resp.data != null) {
                    _state.value = normalizeAvatarUrl(resp.data)
                } else {
                    _events.emit(resp.message ?: "Failed to load settings")
                    println("Load Error: ${resp.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit("Failed to load settings")
            }
        }
    }

    /** Normalize relative avatar path */
    private fun normalizeAvatarUrl(dto: AdminSettingsDto): AdminSettingsDto {
        val raw = dto.avatar_url
        val full = when {
            raw.isNullOrBlank() -> null
            raw.startsWith("http://", true) || raw.startsWith("https://", true) -> raw
            else -> {
                val cleanBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
                cleanBase + raw.removePrefix("/")
            }
        }

        return dto.copy(
            avatar_url = full?.let { "$it?t=${System.currentTimeMillis()}" }
        )
    }

    /** Update profile */
    fun updateProfile(req: UpdateAdminSettingsRequest) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "admin_id" to req.admin_id,
                    "full_name" to req.full_name,
                    "email" to req.email,
                    "phone" to req.phone,
                    "business_name" to req.business_name,
                    "address" to req.address
                )

                val resp = repo.updateSettings(body)
                if (resp.success) {
                    _events.emit("Profile updated")
                    load(req.admin_id)
                } else {
                    _events.emit(resp.message ?: "Profile update failed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit("Profile update failed")
            }
        }
    }

    /** Update preferences */
    fun updatePreferences(req: UpdateAdminPrefRequest) {
        viewModelScope.launch {
            try {
                val body = mapOf(
                    "admin_id" to req.admin_id,
                    "new_order_alerts" to req.new_order_alerts,
                    "low_stock_alerts" to req.low_stock_alerts,
                    "ai_insights" to req.ai_insights,
                    "customer_feedback" to req.customer_feedback
                )

                val resp = repo.updatePreferences(body)
                if (resp.success) {
                    _events.emit("Preferences updated")
                    load(req.admin_id)
                } else {
                    _events.emit(resp.message ?: "Preferences update failed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit("Preferences update failed")
            }
        }
    }

    /** Upload avatar */
    fun uploadAvatar(adminId: Int, avatarPart: MultipartBody.Part) {
        viewModelScope.launch {
            try {
                val resp = repo.uploadAvatar(adminId, avatarPart)
                if (resp.success) {
                    _events.emit("Avatar updated")
                    load(adminId)
                } else {
                    _events.emit(resp.message ?: "Avatar upload failed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _events.emit("Avatar upload failed")
            }
        }
    }
}
