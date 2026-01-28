package com.example.floatingflavors.app.feature.auth.presentation.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.core.auth.TokenManager
import com.example.floatingflavors.app.core.network.NetworkClient
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.floatingflavors.app.core.UserSession
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

sealed class SplashEvent {
    object Idle : SplashEvent()
    data class NavigateToHome(val role: String) : SplashEvent()
    object NavigateToLogin : SplashEvent()
}

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    var splashEvent by mutableStateOf<SplashEvent>(SplashEvent.Idle)
        private set

    fun checkAutoLogin() {
        viewModelScope.launch {
            // Artificial delay for branding (optional, but requested implicitly by existing delay)
            delay(1500)

            val tm = TokenManager.get(getApplication())
            
            // Check Remember Me
            if (!tm.getRememberMe()) {
                tm.clearTokens()
                splashEvent = SplashEvent.NavigateToLogin
                return@launch
            }

            val token = tm.getAccessToken()

            if (token.isNullOrBlank()) {
                splashEvent = SplashEvent.NavigateToLogin
                return@launch
            }

            // Verify Token & Get Role from Backend
            try {
                val storedId = tm.getUserId()
                val response = NetworkClient.homeApi.getHome(userId = storedId)
                
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (data != null) {
                        val role = data.userStats.role
                        val userId = data.userStats.userId
                        
                        // RE-SAVE GLOBAL SESSION
                        UserSession.userId = userId
                        tm.saveRole(role)

                        splashEvent = SplashEvent.NavigateToHome(role)
                    } else {
                        // Data missing -> invalid
                        splashEvent = SplashEvent.NavigateToLogin
                    }
                } else {
                    // Token expired or invalid
                    tm.clearTokens()
                    splashEvent = SplashEvent.NavigateToLogin
                }
            } catch (e: Exception) {
                // Network error or server down -> Fallback to Login
                splashEvent = SplashEvent.NavigateToLogin
            }
        }
    }
}
