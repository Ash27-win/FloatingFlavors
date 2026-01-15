package com.example.floatingflavors.app.feature.auth.data

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.auth.data.remote.dto.ApiResponse
import com.example.floatingflavors.app.feature.auth.data.remote.dto.ForgotPasswordRequest
import com.example.floatingflavors.app.feature.auth.data.remote.dto.LoginRequestDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.LoginResponseDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.RegisterRequestDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.ResetPasswordRequest
import com.example.floatingflavors.app.feature.auth.data.remote.dto.SimpleResponseDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.VerifyOtpRequest

class AuthRepository {

    private val api = NetworkClient.authApi

    suspend fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String
    ): SimpleResponseDto {
        val body = RegisterRequestDto(
            name = name,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            role = role
        )
        return api.register(body)
    }

    suspend fun login(
        email: String,
        password: String,
        role: String
    ): LoginResponseDto {
        val body = LoginRequestDto(
            email = email,
            password = password,
            role = role
        )
        return api.login(body)
    }

    suspend fun sendOtp(email: String): ApiResponse {
        return api.sendOtp(ForgotPasswordRequest(email))
    }


    suspend fun verifyOtp(email: String, otp: String): ApiResponse {
        return api.verifyOtp(VerifyOtpRequest(email, otp))
    }


    suspend fun resetPassword(email: String, password: String): ApiResponse {
        return api.resetPassword(
            ResetPasswordRequest(
                email = email,
                password = password,
                confirm_password = password
            )
        )
    }
}