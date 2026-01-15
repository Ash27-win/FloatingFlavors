package com.example.floatingflavors.app.feature.auth.data.remote.dto
// Requests

data class ForgotPasswordRequest(
    val email: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

data class ResetPasswordRequest(
    val email: String,
    val password: String,
    val confirm_password: String
)

// Generic API Response

data class ApiResponse(
    val success: Boolean,
    val message: String
)
