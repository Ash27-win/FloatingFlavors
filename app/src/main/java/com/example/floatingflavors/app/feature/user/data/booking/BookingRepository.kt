package com.example.floatingflavors.app.feature.user.data.booking

import com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingDto
import com.example.floatingflavors.app.feature.user.data.booking.dto.BookingRequest
import com.example.floatingflavors.app.feature.user.data.booking.dto.BookingResponseDto
import retrofit2.HttpException

class BookingRepository(
    private val api: BookingApi
) {

    suspend fun createBooking(request: BookingRequest): BookingResponseDto {
        return api.createBooking(
            userId = request.userId,
            bookingType = request.bookingType,
            eventType = request.eventType,
            eventName = request.eventName,
            peopleCount = request.peopleCount,
            eventDate = request.eventDate,
            eventTime = request.eventTime,
            companyName = request.companyName,
            contactPerson = request.contactPerson,
            employeeCount = request.employeeCount,
            contractDuration = request.contractDuration,
            serviceFrequency = request.serviceFrequency,
            notes = request.notes
        )
    }

    suspend fun getUserActiveBooking(userId: Int): AdminBookingDto? {
        return try {
            val response = api.getUserActiveBooking(userId)

            if (response.success && response.has_booking) {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}