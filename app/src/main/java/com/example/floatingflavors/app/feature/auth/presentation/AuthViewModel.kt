package com.example.floatingflavors.app.feature.auth.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.auth.data.AuthRepository
import com.example.floatingflavors.app.feature.auth.data.remote.dto.UserDto
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    // Common UI states
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var loggedInUser by mutableStateOf<UserDto?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    fun login(
        email: String,
        password: String,
        role: String,
        onSuccess: (role: String) -> Unit
    ) {
        if (email.isBlank() || password.isBlank() || role == "Select") {
            errorMessage = "Please fill all fields and select role"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val response = repository.login(email, password, role)
                if (response.success) {
                    loggedInUser = response.data
                    isLoading = false
                    onSuccess(response.data?.role ?: role)
                } else {
                    isLoading = false
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Login failed: ${e.message}"
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String,
        onSuccess: () -> Unit
    ) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
            errorMessage = "Please fill all fields"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
            return
        }

        isLoading = true
        errorMessage = null

        viewModelScope.launch {
            try {
                val response = repository.register(name, email, password, confirmPassword, role)
                if (response.success) {
                    isLoading = false
                    onSuccess()
                } else {
                    isLoading = false
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Registration failed: ${e.message}"
            }
        }
    }
}
