package com.example.floatingflavors.app.feature.user.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.HomeRepository
import com.example.floatingflavors.app.feature.user.data.RepoResult
import com.example.floatingflavors.app.feature.user.presentation.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.stateIn

class UserHomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = HomeRepository()
    private val notificationRepository = com.example.floatingflavors.app.feature.notification.data.NotificationRepository(
        com.example.floatingflavors.app.core.network.NetworkClient.notificationApi,
        com.example.floatingflavors.app.core.data.local.AppDatabase.getDatabase(application)
    )

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState
    
    // ✅ Unread Badge
    val unreadCount: StateFlow<Int> = notificationRepository.unreadCount
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    fun loadHome() {
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch {
            // Sync Notifications
            launch { notificationRepository.refreshNotifications() }
            
            when (val result = repo.fetchHome()) {
                is RepoResult.Success -> {
                    _uiState.value = HomeUiState.Success(result.data)
                }
                is RepoResult.Error -> {
                    _uiState.value = HomeUiState.Error(result.message)
                }
            }
        }
    }

    fun refresh() = loadHome()
}




//package com.example.floatingflavors.app.feature.user.presentation
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.floatingflavors.app.feature.user.data.HomeRepository
//import com.example.floatingflavors.app.feature.user.data.RepoResult
//import com.example.floatingflavors.app.feature.user.data.remote.dto.HomeResponseDto
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//sealed class HomeUiState {
//    object Idle : HomeUiState()
//    object Loading : HomeUiState()
//    data class Success(val data: HomeResponseDto) : HomeUiState()
//    data class Error(val message: String) : HomeUiState()
//}
//
//class UserHomeViewModel(
//    private val repo: HomeRepository = HomeRepository()
//) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
//    val uiState: StateFlow<HomeUiState> = _uiState
//
//    fun loadHome() {
//        _uiState.value = HomeUiState.Loading
//        viewModelScope.launch {
//            try {
//                when (val res = repo.fetchHome()) {
//                    is RepoResult.Success -> _uiState.value = HomeUiState.Success(res.data)
//                    is RepoResult.Error -> _uiState.value = HomeUiState.Error(res.message)
//                }
//            } catch (ex: Exception) {
//                _uiState.value = HomeUiState.Error(ex.localizedMessage ?: "Unknown error")
//            }
//        }
//    }
//
//    fun refresh() = loadHome()
//}
