package com.example.floatingflavors.app.core.network

import android.content.Context
import com.example.floatingflavors.app.core.auth.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val token = TokenManager.get(context).getAccessToken()

        // If no token → proceed normally
        if (token.isNullOrEmpty()) {
            return chain.proceed(original)
        }

        // Add Authorization header
        val newRequest = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }
}
