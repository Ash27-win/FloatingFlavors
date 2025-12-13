package com.example.floatingflavors.app.feature.auth.data

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.auth.data.remote.dto.LoginRequestDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.LoginResponseDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.RegisterRequestDto
import com.example.floatingflavors.app.feature.auth.data.remote.dto.SimpleResponseDto

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
}