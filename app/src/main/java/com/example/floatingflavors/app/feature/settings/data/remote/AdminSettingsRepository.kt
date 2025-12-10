package com.example.floatingflavors.app.feature.admin.data.remote

//import com.example.floatingflavors.app.core.network.ApiResponse
import com.example.floatingflavors.app.feature.admin.data.remote.dto.AdminSettingsDto
import com.example.floatingflavors.app.feature.admin.data.remote.dto.ApiResponse
import okhttp3.MultipartBody

class AdminSettingsRepository(private val api: AdminSettingsApi) {

    suspend fun getSettings(adminId: Int): ApiResponse<AdminSettingsDto> {
        return try {
            api.getSettings(adminId)
        } catch (t: Throwable) {
            t.printStackTrace()
            ApiResponse(success = false, message = t.message, data = null)
        }
    }

    suspend fun updateSettings(body: Map<String, Any>): ApiResponse<Unit> {
        return try {
            api.updateSettings(body)
        } catch (t: Throwable) {
            t.printStackTrace()
            ApiResponse(success = false, message = t.message, data = null)
        }
    }

    suspend fun updatePreferences(body: Map<String, Any>): ApiResponse<Unit> {
        return try {
            api.updatePreferences(body)
        } catch (t: Throwable) {
            t.printStackTrace()
            ApiResponse(success = false, message = t.message, data = null)
        }
    }

    suspend fun uploadAvatar(adminId: Int, part: MultipartBody.Part): ApiResponse<Map<String, String>> {
        return try {
            api.uploadAvatar(adminId, part)
        } catch (t: Throwable) {
            t.printStackTrace()
            ApiResponse(success = false, message = t.message, data = null)
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
