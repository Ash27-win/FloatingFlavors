package com.example.floatingflavors.app.feature.user.data.settings

class AddressRepository(private val api: AddressApi) {

    suspend fun load(userId: Int) = api.getAddresses(userId)
    suspend fun add(
        userId: Int, label: String, house: String,
        area: String, pincode: String, city: String, landmark: String?
    ) = api.addAddress(userId,label,house,area,pincode,city,landmark)

    suspend fun delete(id: Int) = api.deleteAddress(id)
}
