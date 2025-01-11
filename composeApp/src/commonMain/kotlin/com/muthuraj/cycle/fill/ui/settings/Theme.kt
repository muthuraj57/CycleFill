package com.muthuraj.cycle.fill.ui.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * Created by Muthuraj on 11/01/25.
 */
enum class Theme {
    Dark, Light, System
}

val DarkThemeEnabled: ProvidableCompositionLocal<Boolean> = compositionLocalOf { false }

@Composable
fun CycleFillTheme(theme: Theme, content: @Composable () -> Unit) {
    val isDarkThemeEnabled = when (theme) {
        Theme.Dark -> true
        Theme.Light -> false
        Theme.System -> isSystemInDarkTheme()
    }
    CompositionLocalProvider(DarkThemeEnabled provides isDarkThemeEnabled) {
        val (colors, colorScheme) = if (DarkThemeEnabled.current) {
            darkColors() to darkColorScheme()
        } else {
            lightColors() to lightColorScheme()
        }
        MaterialTheme(colors = colors) {
            androidx.compose.material3.MaterialTheme(colorScheme = colorScheme) {
                content()
            }
        }
    }
}