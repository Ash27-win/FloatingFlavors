package com.example.floatingflavors.app.feature.auth.presentation.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ResetPasswordState())
    val state: StateFlow<ResetPasswordState> = _state

    fun resetPassword(email: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val response = repository.resetPassword(email, password)
            _state.value = _state.value.copy(
                loading = false,
                success = response.success,
                message = response.message
            )
        }
    }
}

data class ResetPasswordState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String? = null
)
