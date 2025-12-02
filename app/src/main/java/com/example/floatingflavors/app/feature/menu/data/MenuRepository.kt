package com.example.floatingflavors.app.feature.menu.data

import com.example.floatingflavors.app.core.network.NetworkClient
import com.example.floatingflavors.app.feature.menu.data.remote.AddMenuItemRequest
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuResponseDto
import com.example.floatingflavors.app.feature.menu.data.remote.dto.SimpleResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MenuRepository {
    private val api = NetworkClient.menuApi

    suspend fun getMenu(): MenuResponseDto = api.getMenu()

//    suspend fun addMenuItem(
//        name: String,
//        description: String,
//        price: Double,
//        category: String,
//        imageUrl: String
//    ): SimpleResponseDto {
//        val body = AddMenuItemRequest(name, description, price, category, imageUrl, 1)
//        return api.addMenuItem(body)
//    }

    // upload with image file (File can be null)
    suspend fun addMenuItemWithImage(
        name: String,
        description: String,
        price: Double,
        category: String,
        isAvailable: Int = 1,
        imageFile: File? = null
    ): SimpleResponseDto {
        val nameRb = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descRb = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceRb = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val catRb = category.toRequestBody("text/plain".toMediaTypeOrNull())
        val availRb = isAvailable.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let { file ->
            val mime = "image/*".toMediaTypeOrNull()
            val reqFile = file.asRequestBody(mime)
            MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        return api.addMenuItemWithImage(nameRb, descRb, priceRb, catRb, availRb, imagePart)
    }
}