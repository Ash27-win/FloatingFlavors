package com.example.floatingflavors.app.feature.menu.data

import com.example.floatingflavors.app.core.network.NetworkClient
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

    // GET list
    suspend fun getMenu(): MenuResponseDto = api.getMenu()

    // ADD (with optional image)
    suspend fun addMenuItemWithImage(
        name: String,
        description: String,
        price: Double,
        category: String,
        stock: Int, // ✅ Added Stock
        isAvailable: Int = 1,
        imageFile: File? = null
    ): SimpleResponseDto {
        val nameRb = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descRb = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val priceRb = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val catRb = category.toRequestBody("text/plain".toMediaTypeOrNull())
        val stockRb = stock.toString().toRequestBody("text/plain".toMediaTypeOrNull()) // ✅ Stock RB
        val availRb = isAvailable.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val imagePart = imageFile?.let { file ->
            val mime = "image/*".toMediaTypeOrNull()
            val reqFile = file.asRequestBody(mime)
            MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        return api.addMenuItemWithImage(nameRb, descRb, priceRb, catRb, stockRb, availRb, imagePart)
    }

    // EDIT / UPDATE (with optional image)
    suspend fun updateMenuItemWithImage(
        id: Int,
        name: String?,
        description: String?,
        price: Double?,
        category: String?,
        stock: Int?, // ✅ Added Stock
        isAvailable: Int?,
        imageFile: File? = null
    ): SimpleResponseDto {
        // helper to convert string -> RequestBody
        fun String.toRb(): RequestBody = this.toRequestBody("text/plain".toMediaTypeOrNull())

        val idRb = id.toString().toRb()
        val nameRb = name?.toRb()
        val descRb = description?.toRb()
        val priceRb = price?.toString()?.toRb()
        val catRb = category?.toRb()
        val stockRb = stock?.toString()?.toRb() // ✅ Stock RB
        val availRb = isAvailable?.toString()?.toRb()

        val imagePart: MultipartBody.Part? = imageFile?.let { file ->
            val mime = "image/*".toMediaTypeOrNull()
            val reqFile = file.asRequestBody(mime)
            MultipartBody.Part.createFormData("image", file.name, reqFile)
        }

        return api.updateMenuItemWithImage(
            id = idRb,
            name = nameRb,
            description = descRb,
            price = priceRb,
            category = catRb,
            stock = stockRb, // ✅ Pass stock
            isAvailable = availRb,
            image = imagePart
        )
    }

    // DELETE
    suspend fun deleteMenuItem(id: Int): SimpleResponseDto {
        return api.deleteMenuItem(id)
    }

    // TOGGLE / UPDATE AVAILABILITY
    suspend fun updateMenuAvailability(id: Int, isAvailable: Int): SimpleResponseDto {
        return api.updateMenuAvailability(id, isAvailable)
    }
}
