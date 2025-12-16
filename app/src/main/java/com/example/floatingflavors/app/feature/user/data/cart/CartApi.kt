package com.example.floatingflavors.app.feature.user.data.cart

import com.example.floatingflavors.app.feature.user.data.cart.dto.CartResponseDto
import retrofit2.Response
import retrofit2.http.*

interface CartApi {

    @FormUrlEncoded
    @POST("add_to_cart.php")
    suspend fun addToCart(
        @Field("user_id") userId: Int,
        @Field("menu_item_id") menuItemId: Int,
        @Field("price") price: Int
    ): Response<Map<String, Any>>

    @GET("get_cart.php")
    suspend fun getCart(
        @Query("user_id") userId: Int
    ): Response<CartResponseDto>

    @FormUrlEncoded
    @POST("update_cart_item.php")
    suspend fun updateQuantity(
        @Field("cart_item_id") cartItemId: Int,
        @Field("action") action: String
    ): Response<Map<String, Any>>

    @FormUrlEncoded
    @POST("remove_cart_item.php")
    suspend fun removeItem(
        @Field("cart_item_id") cartItemId: Int
    ): Response<Map<String, Any>>
}
