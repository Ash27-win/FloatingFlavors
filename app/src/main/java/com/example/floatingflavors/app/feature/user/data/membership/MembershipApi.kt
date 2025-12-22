package com.example.floatingflavors.app.feature.user.data.membership

import com.example.floatingflavors.app.feature.user.data.membership.dto.MembershipResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MembershipApi {

    @GET("membership.php")
    suspend fun getMembership(
        @Query("user_id") userId: Int
    ): MembershipResponse
}
