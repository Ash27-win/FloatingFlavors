package com.example.floatingflavors.app.feature.auth.presentation

import android.app.Application
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.auth.data.AuthRepository
import com.example.floatingflavors.app.feature.auth.data.remote.dto.UserDto
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

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
        isRememberMe: Boolean,
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
                if (response.success && response.data != null) {
                    val loginData = response.data
                    val user = loginData.user

                    Log.d("LOGIN", "Login success userId=${user.id}")
                    
                    // SAVE TOKENS & ROLE
                    val tokenManager = com.example.floatingflavors.app.core.auth.TokenManager.get(getApplication())
                    tokenManager.saveTokens(loginData.accessToken, loginData.refreshToken)
                    tokenManager.saveRole(user.role)
                    tokenManager.saveUserId(user.id)
                    tokenManager.saveRememberMe(isRememberMe)

                    // GLOBAL SESSION
                    com.example.floatingflavors.app.core.UserSession.userId = user.id

                    loggedInUser = user
                    isLoading = false
                    onSuccess(user.role)
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

    fun logout() {
        loggedInUser = null
    }
}




//package com.example.floatingflavors.app.feature.auth.presentation
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.floatingflavors.app.core.UserSession
//import com.example.floatingflavors.app.feature.auth.data.AuthRepository
//import com.example.floatingflavors.app.feature.auth.data.remote.dto.UserDto
//import kotlinx.coroutines.launch
//
//class AuthViewModel : ViewModel() {
//
//    private val repository = AuthRepository()
//
//    // Common UI states
//    var isLoading by mutableStateOf(false)
//        private set
//
//    var errorMessage by mutableStateOf<String?>(null)
//        private set
//
//    var loggedInUser by mutableStateOf<UserDto?>(null)
//        private set
//
//    fun clearError() {
//        errorMessage = null
//    }
//
//    fun login(
//        email: String,
//        password: String,
//        role: String,
//        onSuccess: (role: String) -> Unit
//    ) {
//        if (email.isBlank() || password.isBlank() || role == "Select") {
//            errorMessage = "Please fill all fields and select role"
//            return
//        }
//
//        isLoading = true
//        errorMessage = null
//
//        viewModelScope.launch {
//            try {
//                val response = repository.login(email, password, role)
////                if (response.success) {
////                    loggedInUser = response.data
////                    isLoading = false
////                    onSuccess(response.data?.role ?: role)
////                }
//                if (response.success) {
//
//                    loggedInUser = response.data
//
//                    // ✅ SAVE USER ID GLOBALLY (THIS IS THE KEY LINE)
//                    UserSession.userId = response.data?.id ?: 0
//
//                    isLoading = false
//                    onSuccess(response.data?.role ?: role)
//                } else {
//                    isLoading = false
//                    errorMessage = response.message
//                }
//            } catch (e: Exception) {
//                isLoading = false
//                errorMessage = "Login failed: ${e.message}"
//            }
//        }
//    }
//
//    fun register(
//        name: String,
//        email: String,
//        password: String,
//        confirmPassword: String,
//        role: String,
//        onSuccess: () -> Unit
//    ) {
//        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
//            errorMessage = "Please fill all fields"
//            return
//        }
//        if (password != confirmPassword) {
//            errorMessage = "Passwords do not match"
//            return
//        }
//
//        isLoading = true
//        errorMessage = null
//
//        viewModelScope.launch {
//            try {
//                val response = repository.register(name, email, password, confirmPassword, role)
//                if (response.success) {
//                    isLoading = false
//                    onSuccess()
//                } else {
//                    isLoading = false
//                    errorMessage = response.message
//                }
//            } catch (e: Exception) {
//                isLoading = false
//                errorMessage = "Registration failed: ${e.message}"
//            }
//        }
//    }
//}