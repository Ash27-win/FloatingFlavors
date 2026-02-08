package com.example.floatingflavors.app.feature.admin.presentation.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.admin.data.remote.AdminRepository
import com.example.floatingflavors.app.feature.admin.data.remote.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminUserListViewModel(private val repository: AdminRepository) : ViewModel() {

    private val _users = MutableStateFlow<List<UserDto>>(emptyList())
    val users = _users.asStateFlow()

    private val _selectedRole = MutableStateFlow("User") // Default Tab
    val selectedRole = _selectedRole.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    // For search/filtering
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    init {
        fetchUsers("User")
    }
    
    fun onRoleSelected(role: String) {
        _selectedRole.value = role
        fetchUsers(role)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun fetchUsers(role: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _users.value = repository.getUsersByRole(role)
            _isLoading.value = false
        }
    }
}
