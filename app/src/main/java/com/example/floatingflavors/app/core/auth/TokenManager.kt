package com.example.floatingflavors.app.core.auth

import android.content.Context

/**
 * TokenManager
 * ------------
 * Responsible ONLY for storing and retrieving tokens.
 * No networking. No Retrofit. No UI logic.
 *
 * Safe for Play Store.
 */
class TokenManager private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_NAME = "floating_flavors_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"

        @Volatile
        private var INSTANCE: TokenManager? = null

        fun get(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext)
                    .also { INSTANCE = it }
            }
        }
    }
    private val KEY_ROLE = "user_role"

    fun saveRole(role: String) {
        prefs.edit().putString(KEY_ROLE, role).apply()
    }

    fun getRole(): String? {
        return prefs.getString(KEY_ROLE, null)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }
}
