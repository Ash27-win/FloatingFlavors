package com.example.floatingflavors.app.feature.user.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.booking.BookingMenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReviewViewModel(
    private val repo: BookingMenuRepository = BookingMenuRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState = _uiState.asStateFlow()

    fun loadReview(bookingId: Int) {
        viewModelScope.launch {
            try {
                val res = repo.getReview(bookingId)

                _uiState.value = ReviewUiState(
                    loading = false,
                    event = res.event,
                    items = res.items,
                    total = res.total_amount
                )

            } catch (e: Exception) {
                // 🔥 THIS PREVENTS CRASH
                _uiState.value = ReviewUiState(
                    loading = false,
                    event = null,
                    items = emptyList(),
                    total = 0.0
                )

                // Log for debugging
                android.util.Log.e(
                    "REVIEW_API_ERROR",
                    "Failed to load review for bookingId=$bookingId",
                    e
                )
            }
        }
    }
}
