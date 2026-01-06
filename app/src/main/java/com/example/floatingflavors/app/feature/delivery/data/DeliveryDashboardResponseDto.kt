package com.example.floatingflavors.app.feature.delivery.data

data class DeliveryDashboardResponseDto(
    val success: Boolean,
    val delivery_partner_name: String,
    val active_order: OrderDto?,
    val upcoming_orders: List<OrderDto>
)

//data class OrderDto(
//    val id: Int,
//    val customer_name: String?,
//    val status: String,
//    val amount: Int,
//    val pickup_address: String?,
//    val drop_address: String?
//)
