import com.example.floatingflavors.app.feature.delivery.data.remote.ActiveOrderDto
import com.example.floatingflavors.app.feature.delivery.data.remote.UpcomingOrderDto
import com.google.gson.annotations.SerializedName

data class DeliveryDashboardResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("delivery_partner_name") val deliveryPartnerName: String? = null,
    @SerializedName("active_order") val activeOrder: ActiveOrderDto? = null,
    @SerializedName("upcoming_orders") val upcomingOrders: List<UpcomingOrderDto>? = null
)

//data class OrderDto(
//    val id: Int,
//    val customer_name: String?,
//    val status: String,
//    val amount: Int,
//    val pickup_address: String?,
//    val drop_address: String?
//)
