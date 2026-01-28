package com.example.floatingflavors.app.feature.auth.presentation.forgot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.auth.data.AuthRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VerifyOtpViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VerifyOtpState())
    val state: StateFlow<VerifyOtpState> = _state

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (i in 60 downTo 0) {
                _state.value = _state.value.copy(seconds = i)
                delay(1000)
            }
        }
    }

    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true)
            val response = repository.verifyOtp(email, otp)
            _state.value = _state.value.copy(
                loading = false,
                success = response.success,
                message = response.message
            )
        }
    }

    fun resendOtp(email: String) {
        viewModelScope.launch {
            repository.sendOtp(email)
            startTimer()
        }
    }
}

data class VerifyOtpState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val message: String? = null,
    val seconds: Int = 60
)
