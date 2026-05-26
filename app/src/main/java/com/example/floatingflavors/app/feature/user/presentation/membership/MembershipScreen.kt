package com.example.floatingflavors.app.feature.user.presentation.membership

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val userId = com.example.floatingflavors.app.core.UserSession.userId

    // Membership state ViewModel
    val vm: MembershipViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repo = MembershipRepository()
                return MembershipViewModel(repo) as T
            }
        }
    )

    // Transaction & Payment ViewModel
    val paymentVm: PaymentViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PaymentViewModel(context) as T
            }
        }
    )

    var processingRefId by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // ActivityResultLauncher for clean UPI payments
    val upiLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val refId = processingRefId
        if (refId != null) {
            if (result.resultCode == Activity.RESULT_OK) {
                val response = result.data?.getStringExtra("response") ?: ""
                if (response.contains("SUCCESS", true)) {
                    paymentVm.verifyPayment(userId, refId, "SUCCESS")
                } else {
                    paymentVm.setPaymentState(PaymentState.VerificationFailed("Payment failed: $response"))
                }
            } else {
                paymentVm.setPaymentState(PaymentState.PaymentCancelled)
            }
        }
    }

    val paymentState by paymentVm.paymentState.collectAsState(initial = PaymentState.Idle)

    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is PaymentState.Idle -> {
                isProcessing = false
            }
            is PaymentState.CreatingTransaction -> {
                isProcessing = true
            }
            is PaymentState.TransactionCreated -> {
                isProcessing = true
                processingRefId = state.referenceId
                upiLauncher.launch(state.intent)
            }
            is PaymentState.Processing -> {
                isProcessing = true
            }
            is PaymentState.PaymentCancelled -> {
                isProcessing = false
                Toast.makeText(context, "Payment cancelled by user", Toast.LENGTH_LONG).show()
                paymentVm.setPaymentState(PaymentState.Idle)
            }
            is PaymentState.VerificationFailed -> {
                isProcessing = false
                Toast.makeText(context, "Verification Failed: ${state.reason}", Toast.LENGTH_LONG).show()
                paymentVm.setPaymentState(PaymentState.Idle)
            }
            is PaymentState.NetworkError -> {
                isProcessing = false
                Toast.makeText(context, "Connection Error. Please check network connection.", Toast.LENGTH_LONG).show()
                paymentVm.setPaymentState(PaymentState.Idle)
            }
            is PaymentState.Success -> {
                isProcessing = false
                Toast.makeText(context, "Membership Activated Successfully!", Toast.LENGTH_LONG).show()
                vm.load(userId) // reload screen state
                paymentVm.setPaymentState(PaymentState.Idle)
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.load(userId)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFFAFAFA),
            topBar = {
                // Sticky Header
                Surface(
                    color = Color.White,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.size(40.dp),
                                enabled = !isProcessing
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color(0xFF111111)
                                )
                            }

                            Text(
                                text = "Membership Plan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111111),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)
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
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 🔹 ACTIVE PLAN
                vm.state?.currentPlan?.let {
                    item { ActiveMembershipCard(it) }
                }

                // 🔹 SECTION TITLE
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Column {
                        Text(
                            text = "Upgrade Your Experience",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111111)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose a plan that fits your lifestyle",
                            fontSize = 14.sp,
                            color = Color(0xFF777777)
                        )
                    }
                }

                // 🔹 PLANS LIST
                vm.state?.availablePlans?.forEach { plan ->
                    item {
                        val planCode = when (plan.id) {
                            1 -> "MONTHLY"
                            2 -> "QUARTERLY"
                            else -> "ELITE"
                        }
                        MembershipPlanCard(
                            plan = plan,
                            isCurrent = vm.state?.currentPlan?.name == plan.name,
                            enabled = !isProcessing,
                            onUpgradeClick = {
                                paymentVm.subscribeMembership(userId, plan.id, planCode)
                            }
                        )
                    }
                }

                // 🔹 CORPORATE (ALWAYS LAST)
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    CorporateMembershipCard()
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Processing Loading Overlay
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFFFF6B00))
                        Text(
                            text = "Processing transaction...",
                            color = Color(0xFF111111),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
