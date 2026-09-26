package com.profilegate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ProfileGateColors = darkColorScheme(
    background = Palette.deepTide,
    surface = Palette.deepTideLighter,
    primary = Palette.signalBlue,
    secondary = Palette.flareTangerine,
    error = Palette.emberCrimson,
    onBackground = Palette.seaGlass,
    onSurface = Palette.seaGlass,
    onPrimary = Palette.deepTide,
)

@Composable
fun ProfileGateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ProfileGateColors,
        content = content,
    )
}
