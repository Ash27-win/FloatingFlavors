package com.example.floatingflavors.app.feature.settings.data.remote.dto

data class UpdateAdminPrefRequest(
    val admin_id: Int,
    val new_order_alerts: Int,
    val low_stock_alerts: Int,
    val ai_insights: Int,
    val customer_feedback: Int
)
