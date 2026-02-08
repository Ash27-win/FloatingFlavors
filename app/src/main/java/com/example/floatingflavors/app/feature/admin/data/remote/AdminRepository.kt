package com.example.floatingflavors.app.feature.admin.data.remote

import android.util.Log

class AdminRepository(private val api: AdminApi) {

    suspend fun getUsersByRole(role: String): List<UserDto> {
        return try {
            val response = api.getUsersByRole(role)
            if (response.success) {
                response.data
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e("AdminRepo", "Error fetching users for role $role: ${e.message}")
            emptyList()
        }
    }

    suspend fun getUserDetails(userId: String): UserDetailsResponse? {
        return try {
            val response = api.getUserDetails(userId)
            if (response.success) response else null
        } catch (e: Exception) {
            Log.e("AdminRepo", "Error fetching details for $userId: ${e.message}")
            null
        }
    }
}
