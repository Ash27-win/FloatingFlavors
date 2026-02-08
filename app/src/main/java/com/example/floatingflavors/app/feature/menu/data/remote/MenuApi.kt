//package com.example.floatingflavors.app.feature.menu.data.remote
//
//import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuResponseDto
//import com.example.floatingflavors.app.feature.menu.data.remote.dto.SimpleResponseDto
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import retrofit2.http.*
//
//// Request DTO for JSON (simple add without image)
//data class AddMenuItemRequest(
//    val name: String,
//    val description: String,
//    val price: Double,
//    val category: String,
//    val imageUrl: String,
//    val isAvailable: Int = 1
//)
//
//interface MenuApi {
//    @GET("get_menu.php")
//    suspend fun getMenu(): MenuResponseDto
//
//    // JSON add (no image)
////    @POST("add_menu_item.php")
////    suspend fun addMenuItem(@Body body: AddMenuItemRequest): SimpleResponseDto
//
//    // Multipart add (file upload)
//    @Multipart
//    @POST("add_menu_item.php")
//    suspend fun addMenuItemWithImage(
//        @Part("name") name: RequestBody,
//        @Part("description") description: RequestBody,
//        @Part("price") price: RequestBody,
//        @Part("category") category: RequestBody,
//        @Part("isAvailable") isAvailable: RequestBody,
//        @Part image: MultipartBody.Part? // optional
//    ): SimpleResponseDto
//}

package com.example.floatingflavors.app.feature.menu.data.remote

import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuResponseDto
import com.example.floatingflavors.app.feature.menu.data.remote.dto.SimpleResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface MenuApi {

    // GET all menu items
    @GET("get_menu.php")
    suspend fun getMenu(): MenuResponseDto

    // Add new menu item with optional image (multipart/form-data)
    @Multipart
    @POST("add_menu_item.php")
    suspend fun addMenuItemWithImage(
        @Part("name") name: RequestBody,
        @Part("description") description: RequestBody,
        @Part("price") price: RequestBody,
        @Part("category") category: RequestBody,
        @Part("stock") stock: RequestBody, // ✅ Added Stock
        @Part("is_available") isAvailable: RequestBody,
        @Part image: MultipartBody.Part? // optional
    ): SimpleResponseDto

    // Update existing menu item (multipart/form-data). Image optional.
    @Multipart
    @POST("update_menu_item.php")
    suspend fun updateMenuItemWithImage(
        @Part("id") id: RequestBody,
        @Part("name") name: RequestBody?,
        @Part("description") description: RequestBody?,
        @Part("price") price: RequestBody?,
        @Part("category") category: RequestBody?,
        @Part("stock") stock: RequestBody?, // ✅ Added Stock
        @Part("is_available") isAvailable: RequestBody?, // use this form name; Kotlin param is isAvailable
        @Part image: MultipartBody.Part? // optional
    ): SimpleResponseDto

    // Delete menu item (form-urlencoded)
    @FormUrlEncoded
    @POST("delete_menu_item.php")
    suspend fun deleteMenuItem(@Field("id") id: Int): SimpleResponseDto

    // Toggle / update availability (form-urlencoded)
    @FormUrlEncoded
    @POST("update_menu_availability.php")
    suspend fun updateMenuAvailability(
        @Field("id") id: Int,
        @Field("is_available") isAvailable: Int
    ): SimpleResponseDto
}

