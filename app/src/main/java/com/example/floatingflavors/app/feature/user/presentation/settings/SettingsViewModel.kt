package com.example.floatingflavors.app.feature.user.presentation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.settings.UserSettingsRepository

import kotlinx.coroutines.launch

class SettingsViewModel(
    application: android.app.Application,
    private val repository: UserSettingsRepository
) : androidx.lifecycle.AndroidViewModel(application) {

    // ✅ MAKE STATE OBSERVABLE
    var uiState by mutableStateOf<SettingsUiState>(SettingsUiState.Loading)
        private set

    fun loadSettings() {
        viewModelScope.launch {
            try {
                val userId = com.example.floatingflavors.app.core.UserSession.userId
                val response = repository.fetchSettings(userId)
                uiState = SettingsUiState.Success(response)
            } catch (e: Exception) {
                uiState = SettingsUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            val userId = com.example.floatingflavors.app.core.UserSession.userId
            repository.toggleNotification(userId, enabled)
            loadSettings()
        }
    }

    fun changeLanguage(language: String) {
        viewModelScope.launch {
            val userId = com.example.floatingflavors.app.core.UserSession.userId
            repository.updateLanguage(userId, language)
            loadSettings()
        }
    }

    fun logout(onLogoutSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = com.example.floatingflavors.app.core.UserSession.userId
                repository.logout(userId)
                
                // 🔥 CLEAR SESSION
                com.example.floatingflavors.app.core.auth.TokenManager.get(getApplication()).clearTokens()
                com.example.floatingflavors.app.core.UserSession.userId = 0
                
                onLogoutSuccess()
            } catch (e: Exception) {
                // Force logout anyway on error
                com.example.floatingflavors.app.core.auth.TokenManager.get(getApplication()).clearTokens()
                com.example.floatingflavors.app.core.UserSession.userId = 0
                onLogoutSuccess()
            }
        }
    }


    fun deleteAccount() {
        viewModelScope.launch {
            val userId = com.example.floatingflavors.app.core.UserSession.userId
            repository.deleteAccount(userId)
        }
    }
}



