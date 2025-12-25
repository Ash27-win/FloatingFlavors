package com.example.floatingflavors.app.feature.user.data.booking

import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingResponse
import com.example.floatingflavors.app.feature.user.data.booking.dto.BookingResponseDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.CommonResponseDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.MenuBookingResponseDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.SaveBookingMenuRequestDto
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface BookingApi {

    @FormUrlEncoded
    @POST("create_event_booking.php")
    suspend fun createBooking(
        @Field("user_id") userId: Int,
        @Field("booking_type") bookingType: String,

        @Field("event_type") eventType: String?,
        @Field("event_name") eventName: String?,
        @Field("people_count") peopleCount: Int?,
        @Field("event_date") eventDate: String?,
        @Field("event_time") eventTime: String?,

        @Field("company_name") companyName: String?,
        @Field("contact_person") contactPerson: String?,
        @Field("employee_count") employeeCount: Int?,
        @Field("contract_duration") contractDuration: String?,
        @Field("service_frequency") serviceFrequency: String?,
        @Field("notes") notes: String?
    ): BookingResponseDto

    @GET("get_user_active_booking.php")
    suspend fun getUserActiveBooking(
        @Query("user_id") userId: Int
    ): AdminBookingResponse

    // BOOKING MENU API
    @GET("get_menu_for_booking.php")
    suspend fun getMenuForBooking(): MenuBookingResponseDto

    @POST("save_booking_menu.php")
    suspend fun saveBookingMenu(
        @Body request: SaveBookingMenuRequestDto
    ): CommonResponseDto

}
