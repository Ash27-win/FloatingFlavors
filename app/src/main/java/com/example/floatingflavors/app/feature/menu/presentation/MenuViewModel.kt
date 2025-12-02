// File: app/src/main/java/com/example/floatingflavors/app/feature/menu/presentation/MenuViewModel.kt
package com.example.floatingflavors.app.feature.menu.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.menu.data.MenuRepository
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import kotlinx.coroutines.launch
import java.io.File

class MenuViewModel : ViewModel() {
    private val repository = MenuRepository()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var menuItems by mutableStateOf<List<MenuItemDto>>(emptyList())
        private set

    fun loadMenu() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = repository.getMenu()
                if (response.success) {
                    menuItems = response.data ?: emptyList()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load menu: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Add menu item without image (uses the same multipart endpoint under the hood with null file).
     * Keeps your UI code simple: call addMenuItem(...) when there's no image file.
     */
    fun addMenuItem(
        name: String,
        description: String,
        price: Double,
        category: String,
        onSuccess: () -> Unit
    ) {
        // delegate to multipart upload method with null file
        addMenuItemWithImage(name, description, price, category, null, onSuccess)
    }

    /**
     * Add menu item with optional image file.
     * imageFile can be null — repository will handle nullable file and call the multipart endpoint accordingly.
     */
    fun addMenuItemWithImage(
        name: String,
        description: String,
        price: Double,
        category: String,
        imageFile: File?,
        onSuccess: () -> Unit
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = repository.addMenuItemWithImage(
                    name = name,
                    description = description,
                    price = price,
                    category = category,
                    isAvailable = 1,
                    imageFile = imageFile
                )
                if (response.success) {
                    onSuccess()
                    loadMenu()
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                errorMessage = "Failed to add item: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
