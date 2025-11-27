package com.example.floatingflavors.app.core.ui.theme

// core/ui/theme/Theme.kt

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF00C853),      // green like your figma button
    secondary = Color(0xFF8BC34A)
)

@Composable
fun FloatingFlavorsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
