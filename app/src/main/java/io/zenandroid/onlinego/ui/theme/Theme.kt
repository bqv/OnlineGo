package io.zenandroid.onlinego.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.MaterialTheme as Material3Theme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = salmon,
    primaryVariant = accent,
    secondary = brownMedium,
    secondaryVariant = salmon,
    background = background,
    onSurface = brown,
    onBackground = brown.copy(alpha = 0.8f),
    onPrimary = lightOnPrimary,
    onSecondary = lightOnPrimary,

    /* Other default colors to override
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
    */
)

private val LightColorScheme = lightColorScheme(
    primary = LightColorPalette.primary,
    primaryContainer = LightColorPalette.primaryVariant,
    secondary = LightColorPalette.secondary,
    secondaryContainer = LightColorPalette.secondaryVariant,
    onSurface = LightColorPalette.onSurface,
    background = LightColorPalette.background,
    onBackground = LightColorPalette.onBackground,
    onPrimary = LightColorPalette.onPrimary,
    onSecondary = LightColorPalette.onSecondary,
)

private val DarkColorPalette = darkColors(
    primary = nightBlue,
    primaryVariant = darkBlue,
    secondary = salmon,
    secondaryVariant = darkBlue,
    surface = nightSurface,
    onSurface = nightOnSurface,
    background = nightBackground,
    onBackground = nightOnBackground,
    onPrimary = nightOnPrimary,
    onSecondary = nightOnPrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkColorPalette.primary,
    secondary = DarkColorPalette.primaryVariant,
    tertiary = DarkColorPalette.secondary,
    surface = DarkColorPalette.surface,
    onSurface = DarkColorPalette.onSurface,
    background = DarkColorPalette.background,
    onBackground = DarkColorPalette.onBackground,
    onPrimary = DarkColorPalette.onPrimary,
    onSecondary = DarkColorPalette.onSecondary,
)

@Composable
fun OnlineGoTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    Material3Theme(
        colorScheme = colorScheme,
        typography = typography3,
        shapes = shapes3,
        content = @Composable {
            MaterialTheme(
                colors = colors,
                typography = typography,
                shapes = shapes,
                content = content
            )
        }
    )
}
