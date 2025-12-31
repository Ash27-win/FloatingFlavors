package com.example.floatingflavors.app.feature.user.presentation.order

data class UserOrderUiModel(
    val orderId: String,
    val dateTime: String,
    val items: List<String>,
    val status: String,
    val amount: String,
    val isEvent: Boolean
)

