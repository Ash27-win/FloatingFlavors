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

class AdminSettingsViewModel(
    private val repo: AdminSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<AdminSettingsDto?>(null)
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    private val baseUrl = NetworkClient.BASE_URL

    fun load(adminId: Int) = viewModelScope.launch {
        val resp = repo.getSettings(adminId)
        if (resp.status && resp.data != null) {
            _state.value = normalizeAvatarUrl(resp.data)
        } else {
            _events.emit(resp.message)
        }
    }

    fun updateProfile(req: UpdateAdminSettingsRequest) = viewModelScope.launch {
        val resp = repo.updateSettings(req)
        if (resp.status) {
            _events.emit("Profile updated")
            load(req.admin_id)
        } else {
            _events.emit(resp.message)
        }
    }

    fun updatePreferences(req: UpdateAdminPrefRequest) = viewModelScope.launch {
        val resp = repo.updatePreferences(req)
        if (resp.status) {
            _events.emit("Preferences updated")
            load(req.admin_id)
        } else {
            _events.emit(resp.message)
        }
    }

    fun uploadAvatar(adminId: Int, avatar: MultipartBody.Part) = viewModelScope.launch {
        val resp = repo.uploadAvatar(adminId, avatar)
        if (resp.status) {
            _events.emit("Avatar updated")
            load(adminId)
        } else {
            _events.emit(resp.message)
        }
    }

    private fun normalizeAvatarUrl(dto: AdminSettingsDto): AdminSettingsDto {
        val raw = dto.avatar_url
        val full = when {
            raw.isNullOrBlank() -> null
            raw.startsWith("http", true) -> raw
            else -> baseUrl.trimEnd('/') + "/" + raw.trimStart('/')
        }
        return dto.copy(avatar_url = full)
    }
}
