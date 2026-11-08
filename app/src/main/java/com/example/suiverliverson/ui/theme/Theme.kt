package com.example.suiverliverson.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private val LiversonDarkColors = darkColorScheme(
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

private val LiversonLightColors = lightColorScheme(
    primary = Red,
    onPrimary = Color.White,
    secondary = Green,
    onSecondary = Color(0xFF062117),
    tertiary = Purple,
    background = Color.White,
    onBackground = Color(0xFF172033),
    surface = Color(0xFFF1F4F8),
    onSurface = Color(0xFF172033),
    surfaceVariant = Color(0xFFE6EAF0),
    onSurfaceVariant = Color(0xFF485568),
    outline = Color(0xFF8994A4),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val LiversonShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun LiversonTheme(darkTheme: Boolean = true, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) LiversonDarkColors else LiversonLightColors,
        typography = Typography,
        shapes = LiversonShapes,
        content = content
    )
}
