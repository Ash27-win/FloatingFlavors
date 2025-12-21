package com.example.floatingflavors.app.feature.user.presentation.settings

sealed class EditProfileUiState {
    object Idle : EditProfileUiState()
    object Loading : EditProfileUiState()
    data class Success(val message: String) : EditProfileUiState()
    data class Error(val message: String) : EditProfileUiState()
}


