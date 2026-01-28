package com.example.floatingflavors.app.feature.user.presentation.membership

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.floatingflavors.app.feature.user.data.membership.MembershipRepository
import com.example.floatingflavors.app.feature.user.presentation.membership.components.ActiveMembershipCard
import com.example.floatingflavors.app.feature.user.presentation.membership.components.CorporateMembershipCard
import com.example.floatingflavors.app.feature.user.presentation.membership.components.MembershipPlanCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(
    navController: NavController
) {
    val userId = com.example.floatingflavors.app.core.UserSession.userId

    val vm: MembershipViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = MembershipRepository()
                return MembershipViewModel(repo) as T
            }
        }
    )

    LaunchedEffect(Unit) {
        vm.load(userId)
    }

    Scaffold(
        topBar = {
            // 🔥 CUSTOM COMPACT HEADER (FIGMA STYLE)
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }

                    Text(
                        text = "Membership Plan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // 🔹 ACTIVE PLAN
            vm.state?.currentPlan?.let {
                item { ActiveMembershipCard(it) }
            }

            // 🔹 SECTION TITLE
            item {
                Text(
                    "Upgrade Your Plan",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 🔹 PLANS
            vm.state?.availablePlans?.forEach { plan ->
                item {
                    MembershipPlanCard(
                        plan = plan,
                        isCurrent = vm.state?.currentPlan?.name == plan.name
                    )
                }
            }

            // 🔹 CORPORATE (ALWAYS LAST)
            item {
                Spacer(modifier = Modifier.height(4.dp))
                CorporateMembershipCard()
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

