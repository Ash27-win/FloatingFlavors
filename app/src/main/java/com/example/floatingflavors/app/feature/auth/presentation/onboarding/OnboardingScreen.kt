package com.example.floatingflavors.app.feature.auth.presentation.onboarding
// feature/auth/presentation/onboarding/OnboardingScreen.kt

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.floatingflavors.R

data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            imageRes = R.drawable.img_onboarding_1,
            title = "Premium Quality Food",
            description = "Fresh ingredients prepared by expert chefs daily for your events"
        ),
        OnboardingPage(
            imageRes = R.drawable.img_onboarding_2,
            title = "Fast & Reliable Delivery",
            description = "Track your orders in real time and get food on time, every time"
        ),
        OnboardingPage(
            imageRes = R.drawable.img_onboarding_3,
            title = "Personalised Menus",
            description = "Choose from spicy, classic or dessert options tailored for you"
        )
    )

    var currentPage by remember { mutableStateOf(0) }
    val isLastPage = currentPage == pages.lastIndex
    val page = pages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 🔹 CENTER BLOCK: image + title + description + dots
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = page.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = page.description,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // dots (center)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { index ->
                    val selected = index == currentPage
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(if (selected) 24.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }

        // 🔹 BOTTOM BUTTONS: centered, side-by-side, Figma style
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),  // distance from bottom edge
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { onFinished() },
                shape = RoundedCornerShape(50),
                modifier = Modifier.width(120.dp)   // fixed width like Figma
            ) {
                Text("Skip")
            }

            Spacer(modifier = Modifier.width(16.dp))  // space between buttons

            Button(
                onClick = {
                    if (isLastPage) onFinished() else currentPage++
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier.width(140.dp)   // Next button a bit wider
            ) {
                Text(if (isLastPage) "Get Started" else "Next")
            }
        }
    }
}
