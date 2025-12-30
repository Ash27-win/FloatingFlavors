package com.example.floatingflavors.app.feature.user.data.booking_checkout

import com.example.floatingflavors.app.feature.user.data.booking_checkout.dto.AddressCheckoutDto

class AddressCheckoutRepository(
    private val api: AddressCheckoutApi
) {

    suspend fun getAddresses(userId: Int): List<AddressCheckoutDto> =
        api.getAddresses(userId).data

    suspend fun setDefault(
        userId: Int,
        addressId: Int
    ): Boolean =
        api.setDefaultAddress(userId, addressId).status
}
