package com.example.tasklite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Blue,
    secondary = SkyBlue,
    background = LightGray,
    surface = White,
    onPrimary = White,
    onBackground = DarkText,
    onSurface = DarkText
)

@Composable
fun TaskLiteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}

