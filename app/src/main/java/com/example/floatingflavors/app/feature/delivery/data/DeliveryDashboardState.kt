package com.example.floatingflavors.app.feature.delivery.data

data class DeliveryDashboardState(
    val deliveryPartnerName: String,
    val activeOrder: OrderDto?,
    val upcomingOrders: List<OrderDto>
)
