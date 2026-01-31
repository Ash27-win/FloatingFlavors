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
            // Artificial delay for branding
            delay(1500)

            val tm = TokenManager.get(getApplication())
            
            // 1. Check "Remember Me"
            val isRemember = tm.getRememberMe()
            android.util.Log.d("SPLASH", "RememberMe: $isRemember")
            
            if (!isRemember) {
                tm.clearTokens()
                splashEvent = SplashEvent.NavigateToLogin
                return@launch
            }

            // 2. Check Token Existence
            val token = tm.getAccessToken()
            val savedRole = tm.getRole()
            val userId = tm.getUserId()

            android.util.Log.d("SPLASH", "Check: Token=${token?.take(5)}... Role=$savedRole UserId=$userId")

            if (token.isNullOrBlank() || savedRole.isNullOrBlank() || userId == 0) {
                android.util.Log.e("SPLASH", "Missing Credentials! Going to Login.")
                splashEvent = SplashEvent.NavigateToLogin
                return@launch
            }

            // 3. Role-Based Verification
            android.util.Log.d("SPLASH", "Verifying Role: $savedRole")
            
            if (savedRole == "Admin" || savedRole == "Delivery") {
                android.util.Log.d("SPLASH", "Admin/Delivery Detected -> Restore Session")
                // ✅ For Admin/Delivery: Restore Session & Navigate directly
                UserSession.userId = userId
                syncFcmToken() // Sync Token
                splashEvent = SplashEvent.NavigateToHome(savedRole)
            } else {
                android.util.Log.d("SPLASH", "User Detected -> Calling getHome")
                // ✅ For User: Verify & Refresh Data via Home API
                try {
                    val response = NetworkClient.homeApi.getHome(userId = userId)
                    
                    if (response.isSuccessful && response.body()?.status == "success") {
                        val data = response.body()?.data
                        if (data != null) {
                            UserSession.userId = data.userStats.userId
                            tm.saveRole(data.userStats.role) // Refresh role just in case
                            syncFcmToken() // Sync Token
                            splashEvent = SplashEvent.NavigateToHome(data.userStats.role)
                        } else {
                            splashEvent = SplashEvent.NavigateToLogin
                        }
                    } else {
                        // Token might be expired or API rejected request
                        tm.clearTokens()
                        splashEvent = SplashEvent.NavigateToLogin
                    }
                } catch (e: Exception) {
                    // Network Error:
                    // Option A: Allow offline login if token exists (User Friendly)
                    // Option B: Force Login (Security strict)
                    // Current behavior: Force Login on error to be safe, or we could just trust the token if exception is just connection.
                    // Let's trust the token if it's just a network error to allow "Offline" usage if desired, 
                    // BUT for now, let's Stick to the existing logic: Fail -> Login.
                    splashEvent = SplashEvent.NavigateToLogin
                }
            }
        }
    }

    private fun syncFcmToken() {
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    android.util.Log.w("SPLASH", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                android.util.Log.d("SPLASH", "FCM Token: $token")
                
                // Send to backend
                viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        com.example.floatingflavors.app.feature.auth.data.AuthRepository().updateFcmToken(token)
                        android.util.Log.d("SPLASH", "FCM Token Synced")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
