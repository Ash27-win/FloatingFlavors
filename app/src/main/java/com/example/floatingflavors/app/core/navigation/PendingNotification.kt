package com.example.floatingflavors.app.core.navigation

object PendingNotification {
    var screen: String? = null
    var referenceId: String? = null

    fun hasPending(): Boolean = screen != null

    fun consume(): Pair<String, String>? {
        val s = screen
        val r = referenceId
        screen = null
        referenceId = null
        return if (s != null) s to (r ?: "") else null
    }
}
