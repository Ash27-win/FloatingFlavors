package com.example.floatingflavors.app.feature.user.data.membership.dto

data class MembershipPlanDto(
    val id: Int,
    val name: String,
    val duration_months: Int,
    val price: Double,
    val discount_percent: Int,
    val priority_support: Boolean,
    val free_delivery: Boolean,
    val dedicated_manager: Boolean
)
