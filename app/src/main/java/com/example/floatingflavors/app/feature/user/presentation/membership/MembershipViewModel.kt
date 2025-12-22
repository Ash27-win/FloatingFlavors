package com.example.floatingflavors.app.feature.user.presentation.membership

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.floatingflavors.app.feature.user.data.membership.MembershipRepository
import com.example.floatingflavors.app.feature.user.data.membership.dto.MembershipResponse
import kotlinx.coroutines.launch

class MembershipViewModel(
    private val repo: MembershipRepository
) : ViewModel() {

    var state by mutableStateOf<MembershipResponse?>(null)
        private set

    fun load(userId: Int) {
        viewModelScope.launch {
            state = repo.fetchMembership(userId)
        }
    }
}
