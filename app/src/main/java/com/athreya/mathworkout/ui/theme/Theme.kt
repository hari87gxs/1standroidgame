package com.athreya.mathworkout.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light color scheme for the app.
 * Using simple Material Design colors for compatibility.
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF000000),
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

/**
 * Dark color scheme for the app.
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color(0xFF000000),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onBackground = Color(0xFFE1E2E1),
    onSurface = Color(0xFFE1E2E1)
)

/**
 * AthreyasSumsTheme - The main theme composable for the app.
 * 
 * This composable applies the appropriate color scheme and typography
 * based on the selected theme.
 * 
 * @param themeId The ID of the theme to use. Defaults to "default".
 * @param content The content to apply the theme to.
 */
@Composable
fun AthreyasSumsTheme(
    themeId: String = "default",
    content: @Composable () -> Unit
) {
    val theme = Themes.getThemeById(themeId)
    
    val colorScheme = lightColorScheme(
        primary = theme.primaryColor,
        onPrimary = theme.onPrimaryColor,
        secondary = theme.secondaryColor,
        onSecondary = theme.onSecondaryColor,
        tertiary = theme.tertiaryColor,
        background = theme.backgroundColor,
        onBackground = theme.onBackgroundColor,
        surface = theme.surfaceColor,
        onSurface = theme.onSurfaceColor,
        surfaceVariant = theme.surfaceColor,
        onSurfaceVariant = theme.onSurfaceColor,
        error = theme.errorColor
    )
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = theme.primaryColor.toArgb()
            val isDark = theme.backgroundColor.luminance() < 0.5f
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Helper extension to calculate luminance of a color
 */
private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}