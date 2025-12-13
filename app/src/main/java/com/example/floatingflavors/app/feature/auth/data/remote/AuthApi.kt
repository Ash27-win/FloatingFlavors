package com.example.floatingflavors.app.feature.auth.data.remote

import com.example.floatingflavors.app.feature.auth.data.remote.dto.LoginRequestDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.LoginResponseDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.RegisterRequestDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.SimpleResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("register.php")
    suspend fun register(
        @Body body: RegisterRequestDto
    ): SimpleResponseDto

    @POST("login.php")
    suspend fun login(
        @Body body: LoginRequestDto
    ): LoginResponseDto
}