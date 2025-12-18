package com.example.floatingflavors.app.feature.user.presentation.settings

import com.example.floatingflavors.app.feature.user.data.settings.dto.UserSettingsDto

sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val data: UserSettingsDto) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}


