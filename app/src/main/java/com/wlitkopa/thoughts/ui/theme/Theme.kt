package com.wlitkopa.thoughts.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Coffee,
    onPrimary = White,
    primaryContainer = CoffeePale,
    onPrimaryContainer = TextOnCoffee,
    secondary = LimeGreen,
    onSecondary = White,
    secondaryContainer = LimeGreenLight,
    onSecondaryContainer = LimeGreenDark,
    tertiary = CoffeeLight,
    onTertiary = CoffeeDark,
    background = LightYellow,
    onBackground = CoffeeDark,
    surface = Cream,
    onSurface = CoffeeDark,
    surfaceVariant = CoffeePale,
    onSurfaceVariant = Coffee,
    outline = CoffeeLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = CoffeeLight,
    onPrimary = CoffeeDark,
    primaryContainer = CoffeeContainer,
    onPrimaryContainer = CoffeePale,
    secondary = LimeGreen,
    onSecondary = White,
    secondaryContainer = LimeGreenDark,
    onSecondaryContainer = LimeGreenLight,
    background = CoffeeSurface,
    onBackground = CoffeePale,
    surface = CoffeeContainer,
    onSurface = CoffeePale,
)

@Composable
fun ThoughtsTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
