package com.example.floatingflavors.app.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light theme tuned to your Figma: white surfaces, light gray background, green primary
private val LightColors = lightColorScheme(
    primary = Color(0xFF00C853),      // your green primary (buttons, accents)
    onPrimary = Color.White,

    secondary = Color(0xFF8BC34A),
    onSecondary = Color.White,

    background = Color(0xFFF4F1F5),   // light gray app background like Figma
    onBackground = Color.Black,

    surface = Color.White,            // white card surfaces
    onSurface = Color.Black,

    surfaceVariant = Color(0xFFF0EAF7),
    onSurfaceVariant = Color(0xFF4A4A4A)
)

@Composable
fun FloatingFlavorsTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}



//package com.example.floatingflavors.app.core.ui.theme
//
//// core/ui/theme/Theme.kt
//
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.graphics.Color
//
//private val LightColors = lightColorScheme(
//    primary = Color(0xFF00C853),      // green like your figma button
//    secondary = Color(0xFF8BC34A)
//)
//
//@Composable
//fun FloatingFlavorsTheme(content: @Composable () -> Unit) {
//    MaterialTheme(
//        colorScheme = LightColors,
//        typography = MaterialTheme.typography,
//        content = content
//    )
//}
