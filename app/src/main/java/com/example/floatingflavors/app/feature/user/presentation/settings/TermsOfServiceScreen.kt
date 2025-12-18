package com.example.floatingflavors.app.feature.user.presentation.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsOfServiceScreen(
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        topBar = {},
        containerColor = Color(0xFFF9FAFB)
    ) { innerPadding ->

        val start = innerPadding.calculateStartPadding(layoutDirection)
        val end = innerPadding.calculateEndPadding(layoutDirection)
        val bottom = innerPadding.calculateBottomPadding()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = start, end = end, bottom = bottom) // ✅ FIX
                .verticalScroll(rememberScrollState())
        ) {

            /* ---------- HEADER ---------- */
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp, max = 72.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Terms of Service",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = TERMS_OF_SERVICE_TEXT,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF374151)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

private const val TERMS_OF_SERVICE_TEXT = """
Welcome to Floating Flavors.

These Terms of Service govern your use of the Floating Flavors mobile application and services.

1. Acceptance of Terms
By accessing or using Floating Flavors, you agree to be bound by these Terms of Service.

2. Services
Floating Flavors provides food catering, event catering, and related services.

3. User Responsibilities
You agree to provide accurate and complete information while using the app.

4. Orders and Payments
All orders placed through the app are subject to acceptance.

5. Cancellations and Refunds
Policies may vary depending on the service selected.

6. Limitation of Liability
Floating Flavors is not responsible for delays beyond reasonable control.

7. Modifications
We may update these terms at any time.

Last updated: December 2025
"""
