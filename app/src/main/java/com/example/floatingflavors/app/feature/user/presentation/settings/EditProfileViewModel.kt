package com.example.floatingflavors.app.feature.user.presentation.settings

import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.settings.EditProfileRepository
import kotlinx.coroutines.launch
import okhttp3.MultipartBody

class EditProfileViewModel(
    private val repo: EditProfileRepository
) : ViewModel() {

    private val USER_ID = 1

    var uiState by mutableStateOf<EditProfileUiState>(EditProfileUiState.Idle)
        private set

    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var altPhone by mutableStateOf("")
    var pincode by mutableStateOf("")
    var city by mutableStateOf("")
    var house by mutableStateOf("")
    var area by mutableStateOf("")
    var landmark by mutableStateOf("")

    var profileImageUrl by mutableStateOf<String?>(null)
    var pickedImageUri by mutableStateOf<Uri?>(null)

    fun fetchProfile() {
        viewModelScope.launch {
            uiState = EditProfileUiState.Loading
            try {
                val res = repo.getProfile(USER_ID)
                res.data?.let { d ->
                    name = d.name
                    phone = d.phone
                    altPhone = d.alt_phone ?: ""
                    profileImageUrl = d.profile_image
                    d.address?.let { a ->
                        pincode = a.pincode
                        city = a.city
                        house = a.house
                        area = a.area
                        landmark = a.landmark ?: ""
                    }
                }
                uiState = EditProfileUiState.Idle
            } catch (e: Exception) {
                uiState = EditProfileUiState.Error("Failed to load profile")
            }
        }
    }

    fun submitProfile(imagePart: MultipartBody.Part?) {
        if (name.isBlank() || phone.length != 10 || pincode.isBlank()) {
            uiState = EditProfileUiState.Error("Fill all required fields correctly")
            return
        }

        viewModelScope.launch {
            uiState = EditProfileUiState.Loading
            try {
                val res = repo.updateProfile(
                    USER_ID,
                    name,
                    phone,
                    altPhone,
                    pincode,
                    city,
                    house,
                    area,
                    landmark,
                    imagePart
                )
                if (res.success) {
                    uiState = EditProfileUiState.Success("Profile updated")
                } else {
                    uiState = EditProfileUiState.Error(res.message ?: "Update failed")
                }
            } catch (e: Exception) {
                uiState = EditProfileUiState.Error(e.message ?: "Server error")
            }
        }
    }
}
