package com.example.floatingflavors.app.feature.user.data.cart

import retrofit2.Response
import retrofit2.http.*

data class CheckoutSummaryResponse(
    val success: Boolean,
    val data: CheckoutData?
)

data class CheckoutData(
    val items: List<CheckoutItem>,
    val total: Int
)

data class CheckoutItem(
    val menu_item_id: Int,
    val name: String,
    val quantity: Int,
    val price: Int,
    val subtotal: Int
)

data class PlaceOrderResponse(
    val success: Boolean,
    val message: String?, // ✅ Added message
    val order_id: Int?
)

interface CheckoutApi {
    @GET("get_checkout_summary.php")
    suspend fun getSummary(@Query("user_id") userId: Int): Response<CheckoutSummaryResponse>

    @FormUrlEncoded
    @POST("place_order.php")
    suspend fun placeOrder(
        @Field("user_id") userId: Int,
        @Field("payment_method") payment: String,
        @Field("user_address_id") addressId: Int
    ): Response<PlaceOrderResponse>
}
