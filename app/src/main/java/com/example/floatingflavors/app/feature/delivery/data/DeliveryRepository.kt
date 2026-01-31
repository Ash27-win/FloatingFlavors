package com.example.floatingflavors.app.feature.delivery.data

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryApi
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryDashboardResponse
import com.example.floatingflavors.app.feature.delivery.data.remote.LiveLocationData as RemoteLiveLocationData
import com.example.floatingflavors.app.feature.delivery.data.remote.OrderTrackingResponse
import com.example.floatingflavors.app.feature.delivery.data.remote.SimpleResponseDto

class DeliveryRepository(
    private val api: DeliveryApi = NetworkClient.retrofit.create(DeliveryApi::class.java)
) {

    /* ---------------- ORDER DETAILS ---------------- */

    suspend fun getOrderDetails(orderId: Int): DeliveryOrderDetailsResponse =
        try {
            api.getOrderDetails(orderId)
        } catch (e: Exception) {
            DeliveryOrderDetailsResponse(
                success = false,
                message = "Failed to fetch order details: ${e.message}",
                data = null
            )
        }

    /* ---------------- ORDER ACTIONS ---------------- */

    suspend fun acceptOrder(orderId: Int, deliveryPartnerId: Int): SimpleResponseDto {
        val res = api.acceptOrder(orderId, deliveryPartnerId)
        return SimpleResponseDto(
            success = res.status,
            message = res.message
        )
    }

    suspend fun rejectOrder(orderId: Int): SimpleResponseDto =
        api.updateOrderStatus(
            orderId = orderId,
            status = "REJECTED"
        )

    suspend fun markAsDelivered(orderId: Int): SimpleResponseDto =
        api.updateOrderStatus(
            orderId = orderId,
            status = "COMPLETED"
        )

    /* ---------------- LIVE LOCATION ---------------- */

    suspend fun updateLiveLocation(
        orderId: Int,
        lat: Double,
        lng: Double,
        deliveryPartnerId: Int
    ): SimpleResponseDto =
        api.updateLiveLocation(orderId, lat, lng, deliveryPartnerId)

    /**
     * ✅ DOMAIN SAFE LOCATION (NO EXTENSIONS)
     */
    suspend fun getLastLiveLocation(orderId: Int): LiveLocationData? =
        try {
            val res = api.getLiveLocation(orderId)
            val remote: RemoteLiveLocationData? = res.location as RemoteLiveLocationData?
            if (res.success && remote != null) {
                LiveLocationData(
                    latitude = remote.latitude,
                    longitude = remote.longitude
                )
            } else null
        } catch (e: Exception) {
            null
        }

    suspend fun getLiveLocation(orderId: Int): LiveLocationResponse =
        api.getLiveLocation(orderId)

    /* ---------------- DASHBOARD ---------------- */

    suspend fun getDashboard(deliveryPartnerId: Int): DeliveryDashboardResponse =
        api.getDashboard(deliveryPartnerId)

    /* ---------------- TRACKING ---------------- */

    suspend fun getOrderTracking(orderId: Int): OrderTrackingResponse =
        api.getOrderTracking(orderId, "INDIVIDUAL")
}
