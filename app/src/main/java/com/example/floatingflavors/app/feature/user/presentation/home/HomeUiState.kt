package com.example.floatingflavors.app.feature.user.presentation.home

import com.example.floatingflavors.app.feature.user.data.remote.dto.HomeResponseDto

sealed class HomeUiState {
    object Idle : HomeUiState()
    object Loading : HomeUiState()
    data class Success(val data: HomeResponseDto) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
