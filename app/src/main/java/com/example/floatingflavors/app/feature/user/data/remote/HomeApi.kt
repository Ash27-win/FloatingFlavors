package com.example.floatingflavors.app.feature.user.data.remote.api

import com.example.floatingflavors.app.feature.user.data.remote.dto.ApiHomeResponse
import com.example.floatingflavors.app.feature.user.data.remote.dto.HomeResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface HomeApi {

    @GET("get_user_home.php")
    suspend fun getHome(
        @Query("user_id") userId: Int
    ): Response<ApiHomeResponse>   // ✅ THIS IS THE KEY FIX
}

