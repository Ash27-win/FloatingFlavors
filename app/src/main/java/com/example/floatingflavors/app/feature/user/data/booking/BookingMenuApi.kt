//package com.example.floatingflavors.app.feature.user.data.booking
//
//import com.example.floatingflavors.app.feature.user.data.booking.dto.CommonResponseDto
//import com.example.floatingflavors.app.feature.user.data.booking.dto.MenuBookingResponseDto
//import com.example.floatingflavors.app.feature.user.data.booking.dto.SaveBookingMenuRequestDto
//import retrofit2.http.Body
//import retrofit2.http.GET
//import retrofit2.http.POST
//
//interface BookingApi {
//
//    @GET("get_menu_for_booking.php")
//    suspend fun getMenuForBooking(): MenuBookingResponseDto
//
//    @POST("save_booking_menu.php")
//    suspend fun saveBookingMenu(
//        @Body request: SaveBookingMenuRequestDto
//    ): CommonResponseDto
//}
