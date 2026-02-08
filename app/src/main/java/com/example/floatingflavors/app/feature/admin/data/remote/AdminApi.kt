package com.example.floatingflavors.app.feature.admin.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AdminApi {
    @GET("get_users_by_role.php")
    suspend fun getUsersByRole(@Query("role") role: String): UserListResponse

    @GET("get_user_details_admin.php")
    suspend fun getUserDetails(@Query("user_id") userId: String): UserDetailsResponse

    @GET("get_admin_analytics.php")
    suspend fun getAnalytics(): AdminAnalyticsResponse

    @POST("update_admin_profile.php")
    suspend fun updateProfile(@Body request: UpdateAdminProfileRequest): SimpleResponse
}

data class AdminAnalyticsResponse(
    val success: Boolean,
    val data: AnalyticsDataDto
)

data class AnalyticsDataDto(
    val total_revenue: Double,
    val total_orders: Int,
    val active_orders: Int,
    val total_users: Int,
    val low_stock_count: Int
)

data class UpdateAdminProfileRequest(
    val admin_id: Int,
    val full_name: String,
    val email: String,
    val phone: String,
    val business_name: String,
    val address: String
)

data class SimpleResponse(val success: Boolean, val message: String?)

data class UserDetailsResponse(
    val success: Boolean,
    val user: UserDto,
    val addresses: List<AddressDto>,
    val stats: UserStatsDto
)

data class AddressDto(
    val id: String,
    val full_address: String?, // Changed from address_line
    val city: String?,
    val label: String?,
    val custom_label: String?
)
data class UserStatsDto(val total_orders: Int, val total_spent: Double, val last_order_date: String?)

data class UserListResponse(
    val success: Boolean,
    val count: Int,
    val role: String,
    val data: List<UserDto>
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val phone: String?,
    val created_at: String?,
    val profile_image_full: String? // Added
)
