package com.example.floatingflavors.app.feature.delivery.data

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryApi
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryDashboardResponse
import com.example.floatingflavors.app.feature.delivery.data.remote.OrderTrackingResponse
import com.example.floatingflavors.app.feature.delivery.data.remote.SimpleResponseDto

class DeliveryRepository(
    private val api: DeliveryApi = NetworkClient.retrofit.create(DeliveryApi::class.java)
) {
    // ✅ Get order details (reuses existing get_order_detail.php)
    suspend fun getOrderDetails(orderId: Int): DeliveryOrderDetailsResponse {
        return try {
            api.getOrderDetails(orderId)
        } catch (e: Exception) {
            DeliveryOrderDetailsResponse(
                success = false,
                message = "Failed to fetch order details: ${e.message}",
                data = null
            )
        }
    }

    // ✅ Accept order (reuses existing update_order_status.php)
    suspend fun acceptOrder(orderId: Int, deliveryPartnerId: Int): SimpleResponseDto {
        return try {
            api.updateOrderStatus(
                orderId = orderId,
                status = "OUT_FOR_DELIVERY",
                deliveryPartnerId = deliveryPartnerId
            )
        } catch (e: Exception) {
            SimpleResponseDto(
                success = false,
                message = "Failed to accept order: ${e.message}"
            )
        }
    }

    // ✅ Reject order (reuses existing update_order_status.php)
    suspend fun rejectOrder(orderId: Int): SimpleResponseDto {
        return try {
            api.updateOrderStatus(
                orderId = orderId,
                status = "REJECTED"
            )
        } catch (e: Exception) {
            SimpleResponseDto(
                success = false,
                message = "Failed to reject order: ${e.message}"
            )
        }
    }

    // ✅ Mark as delivered (reuses existing update_order_status.php)
    suspend fun markAsDelivered(orderId: Int): SimpleResponseDto {
        return try {
            api.updateOrderStatus(
                orderId = orderId,
                status = "COMPLETED"
            )
        } catch (e: Exception) {
            SimpleResponseDto(
                success = false,
                message = "Failed to mark as delivered: ${e.message}"
            )
        }
    }

    // ✅ Update live location (reuses existing update_order_location.php)
    suspend fun updateLiveLocation(
        orderId: Int,
        lat: Double,
        lng: Double,
        deliveryPartnerId: Int
    ): SimpleResponseDto {
        return try {
            api.updateLiveLocation(
                orderId = orderId,
                lat = lat,
                lng = lng,
                deliveryPartnerId = deliveryPartnerId
            )
        } catch (e: Exception) {
            SimpleResponseDto(
                success = false,
                message = "Failed to update location: ${e.message}"
            )
        }
    }

    // ✅ Get live location (reuses existing get_order_location.php)
    suspend fun getLiveLocation(orderId: Int): LiveLocationResponse {
        return try {
            api.getLiveLocation(orderId)
        } catch (e: Exception) {
            LiveLocationResponse(
                success = false,
                message = "Failed to get location: ${e.message}",
                location = null
            )
        }
    }

    // ✅ Get order tracking (reuses existing order_tracking_details.php)
    suspend fun getOrderTracking(orderId: Int): OrderTrackingResponse {
        return try {
            api.getOrderTracking(orderId, "INDIVIDUAL")
        } catch (e: Exception) {
            OrderTrackingResponse(
                success = false,
                message = "Failed to get tracking: ${e.message}",
                currentStatus = null,
                deliveryLocation = null,
                deliveryPerson = null,
                deliveryAddress = null
            )
        }
    }

    // ✅ Get dashboard (uses delivery_dashboard.php)
    suspend fun getDashboard(deliveryPartnerId: Int): DeliveryDashboardResponse {
        return try {
            api.getDashboard(deliveryPartnerId)
        } catch (e: Exception) {
            DeliveryDashboardResponse(
                success = false,
                message = "Failed to get dashboard: ${e.message}",
                deliveryPartnerName = null,
                activeOrder = null,
                upcomingOrders = null
            )
        }
    }
}