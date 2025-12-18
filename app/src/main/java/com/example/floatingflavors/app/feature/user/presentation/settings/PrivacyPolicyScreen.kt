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
fun PrivacyPolicyScreen(
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
                    "Privacy Policy",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = PRIVACY_POLICY_TEXT,
                modifier = Modifier.padding(horizontal = 16.dp),
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF374151)
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
private const val PRIVACY_POLICY_TEXT = """
Floating Flavors respects your privacy and is committed to protecting your personal information.

1. Information We Collect
We may collect personal details such as name, email address, phone number, and order-related information.

2. Use of Information
Your information is used to process orders, improve services, provide customer support, and communicate important updates.

3. Data Security
We implement reasonable technical and organizational measures to protect your data from unauthorized access.

4. Sharing of Information
We do not sell, trade, or rent your personal information to third parties. Data may be shared only when required by law.

5. Third-Party Services
Our app may use trusted third-party services for payments or analytics. These services follow their own privacy policies.

6. User Rights
You have the right to access, update, or request deletion of your personal data.

7. Changes to This Policy
This Privacy Policy may be updated periodically. Any changes will be reflected within the app.

8. Contact Us
For questions regarding this Privacy Policy, please contact Floating Flavors support.

Last updated: December 2025
"""
