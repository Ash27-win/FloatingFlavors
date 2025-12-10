package com.example.floatingflavors.app.feature.admin.data.remote.dto

data class AdminSettingsRemoteDto(
    val admin_id: Int?,
    val full_name: String?,
    val email: String?,
    val new_order_alerts: Boolean? = null,
    val low_stock_alerts: Boolean? = null,
    val ai_insights: Boolean? = null,
    val customer_feedback: Boolean? = null,
    val updated_at: String? = null
)
