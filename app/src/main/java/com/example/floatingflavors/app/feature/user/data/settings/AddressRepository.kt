package com.example.floatingflavors.app.feature.user.data.settings

import com.example.floatingflavors.app.feature.settings.data.remote.dto.ApiResponse
import com.example.floatingflavors.app.feature.user.data.settings.dto.EditAddressRequest

class AddressRepository(private val api: AddressApi) {

    suspend fun load(userId: Int) = api.getAddresses(userId)
    suspend fun add(
        userId: Int, label: String, house: String,
        area: String, pincode: String, city: String, landmark: String?
    ) = api.addAddress(userId,label,house,area,pincode,city,landmark)

    suspend fun updateAddress(req: EditAddressRequest): ApiResponse<Unit> =
        api.updateAddress(
            req.address_id,
            req.user_id,
            req.label,
            req.house,
            req.area,
            req.pincode,
            req.city,
            req.landmark,
            req.is_default
        )

    suspend fun delete(id: Int) = api.deleteAddress(id)

    suspend fun setDefault(addressId: Int, userId: Int) =
        api.setDefaultAddress(addressId, userId)
}
