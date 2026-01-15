package com.example.floatingflavors.app.chatbot.data

data class OrderDto(
    val id: String,
    val status: String,
    val amount: Double,
    val created_at: String
)

data class EventDto(
    val booking_id: String,
    val event_type: String,
    val event_name: String,
    val event_date: String,
    val people_count: Int,
    val status: String
)

data class CorporateDto(
    val booking_id: String,
    val company_name: String,
    val employee_count: Int,
    val contract_duration: String,
    val service_frequency: String,
    val status: String
)
