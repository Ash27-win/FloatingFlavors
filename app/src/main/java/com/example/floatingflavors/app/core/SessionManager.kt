package com.example.floatingflavors.app.core

import android.content.Context

object SessionManager {

    fun saveUserId(context: Context, userId: Int) {
        context.getSharedPreferences("session", Context.MODE_PRIVATE)
            .edit()
            .putInt("user_id", userId)
            .apply()
    }

    fun getUserId(context: Context): Int {
        return context.getSharedPreferences("session", Context.MODE_PRIVATE)
            .getInt("user_id", 0)
    }

    fun clear(context: Context) {
        context.getSharedPreferences("session", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }
}
