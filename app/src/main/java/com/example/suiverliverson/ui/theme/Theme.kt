package com.example.suiverliverson.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LiversonColors = darkColorScheme(
    primary = Red,
    onPrimary = Color(0xFF27100C),
    secondary = Green,
    onSecondary = Color(0xFF062117),
    tertiary = Purple,
    background = AppBackground,
    onBackground = White,
    surface = AppSurface,
    onSurface = White,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
    outline = Color(0xFF3A4A61),
    error = Color(0xFFFFB4A8),
    onError = Color(0xFF690005)
)

private val LiversonShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun LiversonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LiversonColors,
        typography = Typography,
        shapes = LiversonShapes,
        content = content
    )
}
