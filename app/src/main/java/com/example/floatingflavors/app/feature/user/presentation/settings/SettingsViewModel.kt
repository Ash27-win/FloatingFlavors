package com.example.floatingflavors.app.feature.user.presentation.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.settings.UserSettingsRepository

import kotlinx.coroutines.launch

private const val TEMP_USER_ID = 1

class SettingsViewModel(
    private val repository: UserSettingsRepository
) : ViewModel() {

    // ✅ MAKE STATE OBSERVABLE
    var uiState by mutableStateOf<SettingsUiState>(SettingsUiState.Loading)
        private set

    fun loadSettings() {
        viewModelScope.launch {
            try {
                val response = repository.fetchSettings(TEMP_USER_ID)
                uiState = SettingsUiState.Success(response)
            } catch (e: Exception) {
                uiState = SettingsUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }

    fun toggleNotification(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleNotification(TEMP_USER_ID, enabled)
            loadSettings()
        }
    }

    fun changeLanguage(language: String) {
        viewModelScope.launch {
            repository.updateLanguage(TEMP_USER_ID, language)
            loadSettings()
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout(TEMP_USER_ID)
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            repository.deleteAccount(TEMP_USER_ID)
        }
    }
}



