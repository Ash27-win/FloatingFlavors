package com.example.floatingflavors.app.feature.user.data.settings.dto

data class EditProfileResponse(
    val success: Boolean,
    val message: String?,
    val data: ProfileData?
)

data class ProfileData(
    val name: String,
    val phone: String,
    val alt_phone: String?,
    val profile_image: String?,
    val address: AddressData?
)

data class AddressData(
    val pincode: String,
    val city: String,
    val house: String,
    val area: String,
    val landmark: String?
)

