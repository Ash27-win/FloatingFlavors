package com.example.floatingflavors.app.feature.admin.data.remote

import com.example.floatingflavors.app.feature.admin.data.remote.dto.AdminSettingsDto
//import com.example.floatingflavors.app.core.network.ApiResponse
import com.example.floatingflavors.app.feature.admin.data.remote.dto.ApiResponse
import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminPrefRequest
import com.example.floatingflavors.app.feature.settings.data.remote.dto.UpdateAdminSettingsRequest
import okhttp3.MultipartBody
import retrofit2.http.*

interface AdminSettingsApi {

    @GET("get_admin_settings.php")
    suspend fun getSettings(@Query("admin_id") adminId: Int): ApiResponse<AdminSettingsDto>

    @POST("update_admin_settings.php")
    suspend fun updateSettings(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiResponse<Unit>

    @POST("update_admin_prefs.php")
    suspend fun updatePreferences(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiResponse<Unit>

    @Multipart
    @POST("update_admin_avatar.php")
    suspend fun uploadAvatar(
        @Part("admin_id") adminId: Int,
        @Part avatar: MultipartBody.Part
    ): ApiResponse<Map<String, String>> // expects image_full or similar in data
}



//package com.example.floatingflavors.app.feature.admin.data.remote
//
//import com.example.floatingflavors.app.feature.admin.data.remote.dto.AdminSettingsDto
//import com.example.floatingflavors.app.feature.admin.data.remote.dto.ApiResponse
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import retrofit2.http.*
//
//interface AdminSettingsApi {
//    @GET("get_admin_settings.php")
//    suspend fun getSettings(@Query("admin_id") adminId: Int): ApiResponse<AdminSettingsDto>
//
//    @POST("update_admin_settings.php")
//    suspend fun updateSettings(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiResponse<Unit>
//
//    @POST("update_admin_prefs.php")
//    suspend fun updatePreferences(@Body body: Map<String, @JvmSuppressWildcards Any>): ApiResponse<Unit>
//
//    @Multipart
//    @POST("update_admin_avatar.php")
//    suspend fun uploadAvatar(
//        @Part("admin_id") adminId: RequestBody,
//        @Part avatar: MultipartBody.Part
//    ): ApiResponse<Map<String, String>> // expects image_relative/image_full in data
//}
