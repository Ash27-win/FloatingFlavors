package com.example.floatingflavors.app.feature.user.data.membership

import com.example.floatingflavors.app.core.network.NetworkClient

class MembershipRepository {

    private val api = NetworkClient.membershipApi

    suspend fun fetchMembership(userId: Int) =
        api.getMembership(userId)
}
