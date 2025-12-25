package com.example.floatingflavors.app.feature.order.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AdminBookingDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("user_id") val user_id: Int?,
    @SerializedName("booking_type") val booking_type: String?,
    @SerializedName("event_type") val event_type: String?,
    @SerializedName("event_name") val event_name: String?,
    @SerializedName("people_count") val people_count: Int?,
    @SerializedName("event_date") val event_date: String?, // Add this
    @SerializedName("event_time") val event_time: String?,
    @SerializedName("company_name") val company_name: String?,
    @SerializedName("contact_person") val contact_person: String?,
    @SerializedName("employee_count") val employee_count: Int?,
    @SerializedName("contract_duration") val contract_duration: String?,
    @SerializedName("service_frequency") val service_frequency: String?,
    @SerializedName("notes") val notes: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("created_at") val created_at: String?
)