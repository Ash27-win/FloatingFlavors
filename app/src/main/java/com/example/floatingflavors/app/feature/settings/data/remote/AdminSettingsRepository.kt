package com.example.floatingflavors.app.feature.admin.data.remote

import com.example.floatingflavors.app.feature.admin.data.remote.dto.AdminSettingsDto
import com.example.floatingflavors.app.feature.settings.data.remote.dto.ApiResponse
import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminPrefRequest
import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminSettingsRequest
import okhttp3.MultipartBody

class AdminSettingsRepository(private val api: AdminSettingsApi) {

    suspend fun getSettings(adminId: Int): ApiResponse<AdminSettingsDto> =
        safeCall { api.getSettings(adminId) }

    suspend fun updateSettings(req: UpdateAdminSettingsRequest): ApiResponse<Unit> =
        safeCall { api.updateSettings(req) }

    suspend fun updatePreferences(req: UpdateAdminPrefRequest): ApiResponse<Unit> =
        safeCall { api.updatePreferences(req) }

    suspend fun uploadAvatar(
        adminId: Int,
        part: MultipartBody.Part
    ): ApiResponse<Map<String, String>> =
        safeCall { api.uploadAvatar(adminId, part) }

    private inline fun <T> safeCall(
        block: () -> ApiResponse<T>
    ): ApiResponse<T> {
        return try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(
                status = false,
                message = e.message ?: "Network error",
                data = null
            )
        }
    }
}





//package com.example.floatingflavors.app.feature.admin.data.remote
//
//import com.example.floatingflavors.app.feature.admin.data.remote.dto.AdminSettingsDto
//import com.example.floatingflavors.app.feature.admin.data.remote.dto.ApiResponse
//import okhttp3.MediaType.Companion.toMediaTypeOrNull
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.toRequestBody
//
//class AdminSettingsRepository(private val api: AdminSettingsApi) {
//
//    suspend fun getSettings(adminId: Int): ApiResponse<AdminSettingsDto> =
//        api.getSettings(adminId)
//
//    /**
//     * requestMap: build via mapOf("admin_id" to id, "full_name" to name, ...)
//     */
//    suspend fun updateSettings(requestMap: Map<String, Any>): ApiResponse<Unit> =
//        api.updateSettings(requestMap)
//
//    suspend fun updatePreferences(prefMap: Map<String, Any>): ApiResponse<Unit> =
//        api.updatePreferences(prefMap)
//
//    /**
//     * upload avatar. adminId is converted to text/plain RequestBody.
//     * avatarPart built by UI helper (MultipartBody.Part)
//     */
//    suspend fun uploadAvatar(adminId: Int, avatarPart: MultipartBody.Part): ApiResponse<Map<String, String>> {
//        val adminIdBody: RequestBody = adminId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
//        return api.uploadAvatar(adminIdBody, avatarPart)
//    }
//}
