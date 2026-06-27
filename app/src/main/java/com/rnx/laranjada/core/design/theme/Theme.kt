package com.rnx.laranjada.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = LaranjadaOrange,
    secondary = LaranjadaOrangeDark,
    background = LaranjadaBlack,
    surface = LaranjadaSurface,
    onPrimary = LaranjadaText,
    onSecondary = LaranjadaText,
    onBackground = LaranjadaText,
    onSurface = LaranjadaText
)

@Composable
fun LaranjadaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}