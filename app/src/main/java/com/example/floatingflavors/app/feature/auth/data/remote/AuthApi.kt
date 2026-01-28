package com.example.floatingflavors.app.feature.auth.data.remote

import com.example.floatingflavors.app.feature.auth.data.remote.dto.ApiResponse
import com.example.floatingflavors.app.feature.auth.data.remote.dto.ForgotPasswordRequest
import com.example.floatingflavors.app.feature.auth.data.remote.dto.LoginRequestDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.LoginResponseDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.RegisterRequestDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.ResetPasswordRequest
import com.example.floatingflavors.app.feature.auth.data.remote.dto.SimpleResponseDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.VerifyOtpRequest
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

    @POST("api/forgot_password.php")
    suspend fun sendOtp(@Body request: ForgotPasswordRequest): ApiResponse


    @POST("api/verify_otp.php")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): ApiResponse


    @POST("api/reset_password.php")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): ApiResponse
}