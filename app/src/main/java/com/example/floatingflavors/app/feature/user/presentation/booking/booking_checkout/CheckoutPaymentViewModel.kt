package com.example.floatingflavors.app.feature.user.presentation.booking.booking_checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.booking_checkout.PaymentRepository
import kotlinx.coroutines.launch
import java.util.UUID

class CheckoutPaymentViewModel(
    private val repo: PaymentRepository
) : ViewModel() {

    fun markPaymentSuccess(
        bookingId: Int,
        method: String,
        onDone: (txnId: String) -> Unit
    ) {
        val txnId = UUID.randomUUID().toString()

        viewModelScope.launch {
            repo.markPaid(
                bookingId = bookingId,
                txnId = txnId,
                method = method
            )
            onDone(txnId)
        }
    }
}
