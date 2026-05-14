package com.example.floatingflavors.app.feature.delivery.data

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryApi
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryDashboardResponse
import com.example.floatingflavors.app.feature.delivery.data.remote.LiveLocationData as RemoteLiveLocationData
import com.example.floatingflavors.app.feature.delivery.data.remote.OrderTrackingResponse
import com.example.floatingflavors.app.feature.delivery.data.remote.SimpleResponseDto
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryDocumentResponse
import com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryVehicleResponse
import com.example.floatingflavors.app.feature.delivery.data.remote.VehicleUpdateResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    suspend fun acceptOrder(orderId: Int, deliveryPartnerId: Int): SimpleResponseDto =
        try {
            val res = api.acceptOrder(orderId, deliveryPartnerId)
            SimpleResponseDto(success = res.success, message = res.message)
        } catch (e: Exception) {
            SimpleResponseDto(false, "Network Error: ${e.message}")
        }

    suspend fun rejectOrder(orderId: Int, reason: String): SimpleResponseDto =
        try {
            api.updateOrderStatus(
                orderId = orderId,
                status = "REJECTED",
                rejectReason = reason
            )
        } catch (e: Exception) {
            SimpleResponseDto(false, "Network Error: ${e.message}")
        }

    suspend fun markAsDelivered(orderId: Int): SimpleResponseDto =
        try {
            api.updateOrderStatus(
                orderId = orderId,
                status = "COMPLETED"
            )
        } catch (e: Exception) {
            SimpleResponseDto(false, "Network Error: ${e.message}")
        }

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

    /* ---------------- PROFILE ---------------- */

    suspend fun getProfile(): com.example.floatingflavors.app.feature.delivery.data.remote.dto.DeliveryProfileResponse =
        try {
            api.getProfile()
        } catch (e: Exception) {
            com.example.floatingflavors.app.feature.delivery.data.remote.dto.DeliveryProfileResponse(
                success = false,
                profile = null,
                message = "Network error: ${e.message}"
            )
        }

    suspend fun updateProfile(
        name: String?,
        email: String?,
        phone: String?,
        emergencyContact: String?,
        imageFile: java.io.File?
    ): com.example.floatingflavors.app.feature.delivery.data.remote.dto.UpdateDeliveryProfileResponse =
        try {
            val plainTextType = "text/plain".toMediaTypeOrNull()
            val imageType = "image/*".toMediaTypeOrNull()

            val nameBody = name?.toRequestBody(plainTextType)
            val emailBody = email?.toRequestBody(plainTextType)
            val phoneBody = phone?.toRequestBody(plainTextType)
            val emergencyBody = emergencyContact?.toRequestBody(plainTextType)
            
            val imagePart = imageFile?.let {
                val reqFile = it.asRequestBody(imageType)
                okhttp3.MultipartBody.Part.createFormData("profile_image", it.name, reqFile)
            }

            api.updateProfile(nameBody, emailBody, phoneBody, emergencyBody, imagePart)
        } catch (e: Exception) {
            com.example.floatingflavors.app.feature.delivery.data.remote.dto.UpdateDeliveryProfileResponse(
                success = false,
                message = "Update failed: ${e.message}",
                imageUrl = null
            )
        }
    /* ---------------- SETTINGS & COMPLIANCE ---------------- */

    suspend fun getDocuments(): DeliveryDocumentResponse =
        try {
            api.getDocuments()
        } catch (e: Exception) {
            DeliveryDocumentResponse(
                success = false,
                message = "Failed: ${e.message}",
                data = emptyList<com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryDocumentDto>()
            )
        }

    suspend fun uploadDocument(
        type: String,
        docFile: java.io.File,
        mimeType: String = "*/*"
    ): SimpleResponseDto =
        try {
            val plainTextType = "text/plain".toMediaTypeOrNull()
            val fileType = mimeType.toMediaTypeOrNull()

            val typeBody = type.toRequestBody(plainTextType)

            val reqFile = docFile.asRequestBody(fileType)
            val docPart = okhttp3.MultipartBody.Part.createFormData("file", docFile.name, reqFile)

            api.uploadDocument(typeBody, docPart)
        } catch (e: Exception) {
            SimpleResponseDto(false, "Upload failed: ${e.message}")
        }

    suspend fun getVehicleInfo(): DeliveryVehicleResponse =
        try {
            api.getVehicleInfo()
        } catch (e: Exception) {
            DeliveryVehicleResponse(
                success = false,
                message = "Failed: ${e.message}",
                data = null
            )
        }

    suspend fun updateVehicleInfo(request: com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryVehicleUpdateRequest): SimpleResponseDto =
        try {
            api.updateVehicleInfo(request)
        } catch (e: Exception) {
            SimpleResponseDto(false, "Update failed: ${e.message}")
        }

    suspend fun updateVehicleImage(
        vehicleType: String?,
        imageFile: java.io.File
    ): VehicleUpdateResponse =
        try {
            val plainTextType = "text/plain".toMediaTypeOrNull()
            val imageType = "image/*".toMediaTypeOrNull()

            val typeBody = vehicleType?.toRequestBody(plainTextType)
            val reqFile = imageFile.asRequestBody(imageType)
            // Standardize multipart name to match typical PHP server expectation
            val imagePart = okhttp3.MultipartBody.Part.createFormData("vehicle_image", imageFile.name, reqFile)

            api.updateVehicleImage(typeBody, imagePart)
        } catch (e: Exception) {
            VehicleUpdateResponse(false, "Vehicle image update failed: ${e.message}")
        }

    /* ---------------- NOTIFICATIONS ---------------- */

    suspend fun getNotifications(limit: Int = 20, offset: Int = 0): com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryNotificationResponse =
        try {
            api.getNotifications(limit, offset)
        } catch (e: Exception) {
            com.example.floatingflavors.app.feature.delivery.data.remote.DeliveryNotificationResponse(
                success = false,
                message = "Failed: ${e.message}",
                unreadCount = 0,
                notifications = emptyList()
            )
        }

    suspend fun markNotificationRead(id: Int?): SimpleResponseDto =
        try {
            api.markNotificationRead(com.example.floatingflavors.app.feature.delivery.data.remote.MarkNotificationRequest(id))
        } catch (e: Exception) {
            SimpleResponseDto(false, "Failed to mark read: ${e.message}")
        }

    suspend fun deleteNotification(id: Int): SimpleResponseDto =
        try {
            api.deleteNotification(com.example.floatingflavors.app.feature.delivery.data.remote.DeleteNotificationRequest(id))
        } catch (e: Exception) {
            SimpleResponseDto(false, "Failed to delete: ${e.message}")
        }
}
