package com.example.floatingflavors.app.feature.user.data.booking_checkout

class PaymentRepository(
    private val api: PaymentApi
) {
    suspend fun markPaid(
        bookingId: Int,
        txnId: String,
        method: String
    ) {
        api.markPaid(bookingId, txnId, method)
    }
}
