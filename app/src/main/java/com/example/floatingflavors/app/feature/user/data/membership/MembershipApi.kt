package com.example.floatingflavors.app.feature.user.data.membership

import com.example.floatingflavors.app.feature.user.data.membership.dto.MembershipResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field

interface MembershipApi {

    @GET("membership.php")
    suspend fun getMembership(
        @Query("user_id") userId: Int
    ): MembershipResponse

    @FormUrlEncoded
    @POST("subscribe_membership.php")
    suspend fun subscribeMembership(
        @Field("user_id") userId: Int,
        @Field("plan_id") planId: Int
    ): SubscribeMembershipResponse

    @FormUrlEncoded
    @POST("verify_payment.php")
    suspend fun verifyPayment(
        @Field("reference_id") referenceId: String,
        @Field("payment_status") status: String
    ): VerifyPaymentResponse
}

data class SubscribeMembershipResponse(
    val success: Boolean,
    val reference_id: String?,
    val amount: Double?,
    val message: String?
)

data class VerifyPaymentResponse(
    val success: Boolean,
    val status: String?,
    val message: String?
)
