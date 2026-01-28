package com.example.floatingflavors.app.core.network

import android.content.Context
import com.example.floatingflavors.app.core.auth.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val tokenManager = TokenManager.get(context)
        val accessToken = tokenManager.getAccessToken()

        val requestBuilder = chain.request().newBuilder()

        if (!accessToken.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}
