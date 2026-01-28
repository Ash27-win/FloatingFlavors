package com.example.floatingflavors.app.core.network

import android.content.Context
import com.example.floatingflavors.app.core.auth.TokenManager
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import org.json.JSONObject

class TokenAuthenticator(private val context: Context) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 1. Safety check: prevent infinite loops if refresh continuously fails (401 -> Refresh -> 401...)
        if (responseCount(response) >= 2) {
            return null // Give up after 1 retry
        }

        val tokenManager = TokenManager.get(context)
        val refreshToken = tokenManager.getRefreshToken()

        if (refreshToken.isNullOrBlank()) {
            return null // No refresh token, let user be logged out
        }

        // 2. Synchronous Network Call to Refresh Token
        // We use a separate OkHttpClient to avoid circular dependency with this Authenticator
        val refreshClient = OkHttpClient()

        val jsonMediaType = "application/json; charset=utf-8".toMediaType()
        // Payload matches backend expectations: { "refresh_token": "..." }
        val jsonBody = "{\"refresh_token\": \"$refreshToken\"}"

        val refreshRequest = Request.Builder()
            .url(NetworkClient.BASE_URL + "refresh_token.php") // Ensure BASE_URL is accessible
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        try {
            val refreshResponse = refreshClient.newCall(refreshRequest).execute()

            if (refreshResponse.isSuccessful) {
                val bodyString = refreshResponse.body?.string()
                if (!bodyString.isNullOrBlank()) {
                    val json = JSONObject(bodyString)
                    
                    if (json.optBoolean("success")) {
                        val newAccessToken = json.optString("access_token")
                        
                        if (newAccessToken.isNotBlank()) {
                            // 3. Save new token
                            tokenManager.saveTokens(newAccessToken, refreshToken) // Keep existing RT

                            // 4. Retry valid request with new token
                            return response.request.newBuilder()
                                .header("Authorization", "Bearer $newAccessToken")
                                .build()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Refresh failed (Network error, etc). We allow the 401 to propagate.
            e.printStackTrace()
        }

        return null // Refresh failed
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
