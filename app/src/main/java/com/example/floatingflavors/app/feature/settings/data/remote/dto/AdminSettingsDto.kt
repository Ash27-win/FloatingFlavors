package com.example.floatingflavors.app.feature.admin.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AdminSettingsDto(
    @SerializedName("admin_id") val admin_id: Int? = null,
    @SerializedName("full_name") val full_name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("business_name") val business_name: String? = null,
    @SerializedName("address") val address: String? = null,
    // may be relative (uploads/...) or full url
    @SerializedName("avatar_url") val avatar_url: String? = null,
    // notification prefs - server returns booleans
    @SerializedName("new_order_alerts") val new_order_alert: Boolean? = null,
    @SerializedName("low_stock_alerts") val low_stock_alert: Boolean? = null,
    @SerializedName("ai_insights") val ai_insights: Boolean? = null,
    @SerializedName("customer_feedback") val customer_feedback: Boolean? = null,
    @SerializedName("updated_at") val updated_at: String? = null
)



//package com.example.floatingflavors.app.feature.admin.data.remote.dto
//
//data class AdminSettingsDto(
//    val admin_id: Int?,
//    val full_name: String?,
//    val email: String?,
//    val phone: String?,
//    val business_name: String?,
//    val address: String?,
//    val avatar_url: String?,           // relative or full
//    val new_order_alerts: Boolean?,    // <- changed to Boolean
//    val low_stock_alerts: Boolean?,
//    val ai_insights: Boolean?,
//    val customer_feedback: Boolean?,
//    val updated_at: String?
//)
