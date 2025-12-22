package com.example.floatingflavors.app.feature.user.data.membership.dto

data class MembershipResponse(
    val currentPlan: UserMembershipDto?,
    val availablePlans: List<MembershipPlanDto>
)
