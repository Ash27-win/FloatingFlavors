package com.example.floatingflavors.app.feature.user.presentation.booking

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.booking.BookingRepository
import com.example.floatingflavors.app.feature.user.data.booking.dto.BookingRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BookingViewModel(
    private val repo: BookingRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var bookingError by mutableStateOf<String?>(null)
        private set

    var bookingStatus by mutableStateOf<BookingState>(BookingState.Loading)
        private set

    private var statusPollingJob: Job? = null

    /* ---------------- STATE ---------------- */

    sealed class BookingState {
        object Loading : BookingState()
        object None : BookingState()

        data class Active(
            val bookingId: String,
            val bookingType: String,
            val details: String,
            val status: String, // PENDING | CONFIRMED | CANCELLED
            val eventName: String? = null,
            val companyName: String? = null,
            val dateTime: String? = null
        ) : BookingState()
    }

    /* ---------------- CREATE BOOKING ---------------- */

    fun submitBooking(request: BookingRequest, userId: Int) {
        viewModelScope.launch {
            isLoading = true
            bookingError = null

            try {
                val response = repo.createBooking(request)

                if (response.success && response.booking_id != null) {
                    val bookingId = response.booking_id.toString()

                    bookingStatus = BookingState.Active(
                        bookingId = bookingId,
                        bookingType = request.bookingType,
                        details = if (request.bookingType == "EVENT")
                            "${request.eventName} - ${request.peopleCount} people"
                        else
                            "${request.companyName} - ${request.employeeCount} employees",
                        status = "PENDING",
                        eventName = request.eventName,
                        companyName = request.companyName,
                        dateTime = request.eventDate
                    )

                    startStatusPolling(userId, bookingId)
                } else {
                    bookingError = response.message ?: "Booking failed"
                }

            } catch (e: Exception) {
                bookingError = "Network error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /* ---------------- CHECK STATUS (ON LOGIN / RESUME) ---------------- */

    fun checkUserBookingStatus(userId: Int) {
        viewModelScope.launch {
            bookingStatus = BookingState.Loading
            bookingError = null

            try {
                val booking = repo.getUserActiveBooking(userId)

                if (booking == null) {
                    bookingStatus = BookingState.None
                    return@launch
                }

                bookingStatus = mapBookingToState(booking)

                if (booking.status == "PENDING") {
                    startStatusPolling(userId, booking.id.toString())
                }

            } catch (e: Exception) {
                bookingError = "Failed to fetch booking status"
                bookingStatus = BookingState.None
            }
        }
    }

    /* ---------------- POLLING ---------------- */

    private fun startStatusPolling(userId: Int, bookingId: String) {
        statusPollingJob?.cancel()

        statusPollingJob = viewModelScope.launch {
            while (true) {
                delay(8000)

                try {
                    val booking = repo.getUserActiveBooking(userId)

                    if (booking == null || booking.id.toString() != bookingId) {
                        bookingStatus = BookingState.None
                        break
                    }

                    bookingStatus = mapBookingToState(booking)

                    if (booking.status == "CONFIRMED" || booking.status == "CANCELLED") {
                        break
                    }

                } catch (_: Exception) {
                    // keep polling
                }
            }
        }
    }

    /* ---------------- MAPPER ---------------- */

    private fun mapBookingToState(booking: com.example.floatingflavors.app.feature.order.data.remote.dto.AdminBookingDto): BookingState.Active {
        return BookingState.Active(
            bookingId = booking.id?.toString() ?: booking.booking_id?.toString() ?: "0", // FIXED: Handle both id and booking_id
            bookingType = booking.booking_type ?: "UNKNOWN",
            details = if (booking.booking_type == "EVENT")
                "${booking.event_name ?: "Event"} - ${booking.people_count ?: 0} people"
            else
                "${booking.company_name ?: "Company"} - ${booking.employee_count ?: 0} employees",
            status = booking.status ?: "PENDING",
            eventName = booking.event_name,
            companyName = booking.company_name,
            dateTime = booking.event_date
        )
    }

    /* ---------------- CLEAR (ONLY WHEN USER EXPLICITLY RESETS) ---------------- */

    fun clearBooking() {
        statusPollingJob?.cancel()
        bookingStatus = BookingState.None
        bookingError = null
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            // optional: call cancel API
            clearBooking()
        }
    }
}