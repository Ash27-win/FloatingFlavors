package com.example.floatingflavors.app.feature.user.data.order

import com.example.floatingflavors.app.feature.user.data.order.dto.UserOrderDetailResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface UserOrdersApi {

        @GET("get_order_detail.php")
        suspend fun getOrderDetail(
            @Query("id") id: String
        ): UserOrderDetailResponseDto

//        @GET("user_profile_get.php")
//        suspend fun getUserProfile(
//            @Query("user_id") userId: Int
//        ): UserProfileResponseDto
}