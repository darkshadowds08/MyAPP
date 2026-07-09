package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = HighDensityBluePrimary,
    secondary = HighDensityBlueLight,
    tertiary = OrangeWaiting,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = HighDensityBlueDark,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = TextLight,
    onSurface = TextLight,
    primaryContainer = HighDensityBlueDark,
    onPrimaryContainer = HighDensityBlueLight
  )

private val LightColorScheme =
  lightColorScheme(
    primary = HighDensityBluePrimary,
    secondary = HighDensityBlueDark,
    tertiary = OrangeWaiting,
    background = LightBg,
    surface = LightSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark,
    primaryContainer = HighDensityBlueLight,
    onPrimaryContainer = HighDensityBlueDark,
    outlineVariant = CardBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic color to maintain our polished rich teal slate branding
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
