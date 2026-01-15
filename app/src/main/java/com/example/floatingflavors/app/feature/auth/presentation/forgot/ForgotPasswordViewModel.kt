package com.example.floatingflavors.app.feature.auth.presentation.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ForgotPasswordState())
    val state: StateFlow<ForgotPasswordState> = _state

    fun sendOtp(email: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val response = repository.sendOtp(email)
            _state.value = _state.value.copy(
                loading = false,
                success = response.success,
                message = response.message
            )
        }
    }
}

data class ForgotPasswordState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String? = null
)
