package com.habitstreak.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Background,
    secondary = SuccessGreen,
    onSecondary = Background,
    tertiary = StreakOrange,
    background = Background,
    onBackground = DarkText,
    surface = Background,
    onSurface = DarkText,
    surfaceVariant = LightGray,
    outline = BorderGray
)

@Composable
fun HabitStreakTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
