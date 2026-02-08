package com.example.floatingflavors.app.feature.menu.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.menu.data.MenuRepository
import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.io.File

class MenuViewModel(
    private val repository: MenuRepository = MenuRepository()
) : ViewModel() {

    // UI state as mutableStateOf so Compose recomposes when changed
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var menuItems by mutableStateOf<List<MenuItemDto>>(emptyList())
        private set

    // one-off UI events (snackbar messages) — optional to collect
    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events

    // ----- Load menu -----
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
                    // emit event too
                    _events.emit(response.message ?: "Failed to load menu")
                }
            } catch (e: Exception) {
                val msg = "Failed to load menu: ${e.message}"
                errorMessage = msg
                _events.emit(msg)
            } finally {
                isLoading = false
            }
        }
    }

    // ----- Add (with optional image) -----
    fun addMenuItemWithImage(
        name: String,
        description: String,
        price: Double,
        category: String,
        stock: Int, // ✅ Added Stock
        imageFile: File?,
        onSuccess: (() -> Unit)? = null
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val resp = repository.addMenuItemWithImage(
                    name = name,
                    description = description,
                    price = price,
                    category = category,
                    stock = stock, // ✅ Pass Stock
                    isAvailable = 1,
                    imageFile = imageFile
                )
                if (resp.success) {
                    onSuccess?.invoke()
                    loadMenu()
                    _events.emit(resp.message ?: "Added")
                } else {
                    errorMessage = resp.message
                    _events.emit(resp.message ?: "Add failed")
                }
            } catch (e: Exception) {
                val msg = "Failed to add item: ${e.message}"
                errorMessage = msg
                _events.emit(msg)
            } finally {
                isLoading = false
            }
        }
    }

    // ----- Delete -----
    fun deleteMenuItem(id: Int) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val resp = repository.deleteMenuItem(id)
                if (resp.success) {
                    // remove locally to be responsive
                    menuItems = menuItems.filter { it.id?.toIntOrNull() != id }
                    _events.emit(resp.message ?: "Deleted")
                } else {
                    errorMessage = resp.message
                    _events.emit(resp.message ?: "Delete failed")
                }
            } catch (e: Exception) {
                val msg = "Delete failed: ${e.message}"
                errorMessage = msg
                _events.emit(msg)
            } finally {
                isLoading = false
            }
        }
    }

    // ----- Toggle availability (0/1) -----
    // Calls repository.updateMenuAvailability(id, isAvailable)
    fun toggleAvailability(id: Int, isAvailable: Int) {
        // optimistic UI: update locally first
        val oldList = menuItems
        menuItems = menuItems.map {
            if (it.id?.toIntOrNull() == id) it.copy(is_available = isAvailable.toString()) else it
        }

        viewModelScope.launch {
            try {
                val resp = repository.updateMenuAvailability(id, isAvailable)
                if (!resp.success) {
                    // rollback and show error
                    menuItems = oldList
                    errorMessage = resp.message
                    _events.emit(resp.message ?: "Update failed")
                } else {
                    _events.emit(resp.message ?: "Availability updated")
                }
            } catch (e: Exception) {
                // rollback on network error
                menuItems = oldList
                val msg = "Update failed: ${e.message}"
                errorMessage = msg
                _events.emit(msg)
            }
        }
    }

    // ----- Edit (update fields + optional image) -----
    fun editMenuItem(
        id: Int,
        name: String?,
        description: String?,
        price: Double?,
        category: String?,
        stock: Int?, // ✅ Added Stock
        isAvailable: Int?,
        imageFile: File? = null
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val resp = repository.updateMenuItemWithImage(
                    id = id,
                    name = name,
                    description = description,
                    price = price,
                    category = category,
                    stock = stock, // ✅ Pass Stock
                    isAvailable = isAvailable,
                    imageFile = imageFile
                )
                if (resp.success) {
                    loadMenu()
                    _events.emit(resp.message ?: "Updated")
                } else {
                    errorMessage = resp.message
                    _events.emit(resp.message ?: "Update failed")
                }
            } catch (e: Exception) {
                val msg = "Update failed: ${e.message}"
                errorMessage = msg
                _events.emit(msg)
            } finally {
                isLoading = false
            }
        }
    }
}


//package com.example.floatingflavors.app.feature.menu.presentation
//
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.setValue
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.floatingflavors.app.feature.menu.data.MenuRepository
//import com.example.floatingflavors.app.feature.menu.data.remote.dto.MenuItemDto
//import kotlinx.coroutines.launch
//import java.io.File
//
//class MenuViewModel : ViewModel() {
//    private val repository = MenuRepository()
//
//    var isLoading by mutableStateOf(false)
//        private set
//
//    // Expose errorMessage as public read-only property (UI can read it)
//    var errorMessage by mutableStateOf<String?>(null)
//        private set
//
//    var menuItems by mutableStateOf<List<MenuItemDto>>(emptyList())
////        private set
//
//    // Public helper to set error from UI/validation
//    fun setError(message: String?) {
//        errorMessage = message
//    }
//
//    fun clearError() {
//        errorMessage = null
//    }
//
//    fun loadMenu() {
//        isLoading = true
//        errorMessage = null
//        viewModelScope.launch {
//            try {
//                val response = repository.getMenu()
//                if (response.success) {
//                    menuItems = response.data ?: emptyList()
//                } else {
//                    errorMessage = response.message
//                }
//            } catch (e: Exception) {
//                errorMessage = "Failed to load menu: ${e.message}"
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//
//    fun addMenuItem(
//        name: String,
//        description: String,
//        price: Double,
//        category: String,
//        onSuccess: () -> Unit
//    ) {
//        addMenuItemWithImage(name, description, price, category, null, onSuccess)
//    }
//
//    fun addMenuItemWithImage(
//        name: String,
//        description: String,
//        price: Double,
//        category: String,
//        imageFile: File?,
//        onSuccess: () -> Unit
//    ) {
//        isLoading = true
//        errorMessage = null
//        viewModelScope.launch {
//            try {
//                val response = repository.addMenuItemWithImage(
//                    name = name,
//                    description = description,
//                    price = price,
//                    category = category,
//                    isAvailable = 1,
//                    imageFile = imageFile
//                )
//                if (response.success) {
//                    onSuccess()
//                    loadMenu()
//                } else {
//                    errorMessage = response.message
//                }
//            } catch (e: Exception) {
//                errorMessage = "Failed to add item: ${e.message}"
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//
//    fun deleteMenuItem(id: Int) {
//        viewModelScope.launch {
//            try {
//                isLoading = true
//                errorMessage = null
//                val resp = repository.deleteMenuItem(id)
//                if (resp.success) {
//                    loadMenu()
//                } else {
//                    errorMessage = resp.message
//                }
//            } catch (e: Exception) {
//                errorMessage = "Delete failed: ${e.message}"
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//
//    fun toggleAvailability(id: Int, isAvailable: Int) {
//        viewModelScope.launch {
//            try {
//                isLoading = true
//                errorMessage = null
//                val resp = repository.updateMenuAvailability(id, isAvailable)
//                if (resp.success) {
//                    loadMenu()
//                } else {
//                    errorMessage = resp.message
//                }
//            } catch (e: Exception) {
//                errorMessage = "Update failed: ${e.message}"
//            } finally {
//                isLoading = false
//            }
//        }
//    }
//}
