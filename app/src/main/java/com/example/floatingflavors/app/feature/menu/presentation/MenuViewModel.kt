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

    // Expose errorMessage as public read-only property (UI can read it)
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var menuItems by mutableStateOf<List<MenuItemDto>>(emptyList())
        private set

    // Public helper to set error from UI/validation
    fun setError(message: String?) {
        errorMessage = message
    }

    fun clearError() {
        errorMessage = null
    }

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

    fun addMenuItem(
        name: String,
        description: String,
        price: Double,
        category: String,
        onSuccess: () -> Unit
    ) {
        addMenuItemWithImage(name, description, price, category, null, onSuccess)
    }

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

    fun deleteMenuItem(id: Int) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                val resp = repository.deleteMenuItem(id)
                if (resp.success) {
                    loadMenu()
                } else {
                    errorMessage = resp.message
                }
            } catch (e: Exception) {
                errorMessage = "Delete failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun toggleAvailability(id: Int, isAvailable: Int) {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null
                val resp = repository.updateMenuAvailability(id, isAvailable)
                if (resp.success) {
                    loadMenu()
                } else {
                    errorMessage = resp.message
                }
            } catch (e: Exception) {
                errorMessage = "Update failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
