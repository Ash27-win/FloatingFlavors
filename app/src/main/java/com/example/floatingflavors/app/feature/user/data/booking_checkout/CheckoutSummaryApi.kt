package com.example.floatingflavors.app.feature.user.data.booking_checkout

import com.example.floatingflavors.app.feature.user.data.booking_checkout.dto.BookingSummaryResponse
import com.example.floatingflavors.app.feature.user.data.settings.dto.AddressDto
import retrofit2.http.GET
import retrofit2.http.Query

data class AddressListResponse(
    val success: Boolean,
    val data: List<AddressDto>
)

interface CheckoutSummaryApi {

    @GET("get_booking_menu_summary.php")
    suspend fun getBookingSummary(
        @Query("booking_id") bookingId: Int
    ): BookingSummaryResponse

    @GET("user_addresses_get.php")
    suspend fun getAddresses(
        @Query("user_id") userId: Int
    ): AddressListResponse
}
