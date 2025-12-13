package com.example.floatingflavors.app.feature.user.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.HomeRepository
import com.example.floatingflavors.app.feature.user.data.RepoResult
import com.example.floatingflavors.app.feature.user.presentation.home.HomeUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserHomeViewModel : ViewModel() {

    private val repo = HomeRepository()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val uiState: StateFlow<HomeUiState> = _uiState

    fun loadHome() {
        _uiState.value = HomeUiState.Loading

        viewModelScope.launch {
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
