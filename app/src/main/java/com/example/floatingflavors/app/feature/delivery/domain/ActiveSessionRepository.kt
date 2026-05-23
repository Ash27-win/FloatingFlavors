package com.example.floatingflavors.app.feature.delivery.domain

import android.content.Context
import android.content.SharedPreferences

/**
 * Enterprise Delivery Session Recovery System.
 * 
 * Android OS frequently kills apps in the background to save memory.
 * This repository persists the active order ID and Navigation Phase, 
 * so when the rider opens the app again, it instantly resumes the delivery map.
 */
class ActiveSessionRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("delivery_session_prefs", Context.MODE_PRIVATE)

    fun saveActiveSession(orderId: Int, phaseName: String) {
        prefs.edit()
            .putInt("active_order_id", orderId)
            .putString("active_phase", phaseName)
            .apply()
    }

    fun getActiveOrderId(): Int? {
        val id = prefs.getInt("active_order_id", -1)
        return if (id == -1) null else id
    }

    fun getActivePhase(): String? {
        return prefs.getString("active_phase", null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
