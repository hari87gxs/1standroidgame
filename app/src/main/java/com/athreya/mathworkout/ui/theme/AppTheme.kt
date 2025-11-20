package com.athreya.mathworkout.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Theme definition for the app
 */
data class AppTheme(
    val id: String,
    val name: String,
    val description: String,
    val isLocked: Boolean = false,
    val unlockRequirement: String = "",
    val icon: String = "🎨",
    val primaryColor: Color,
    val secondaryColor: Color,
    val tertiaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val errorColor: Color,
    val onPrimaryColor: Color,
    val onSecondaryColor: Color,
    val onBackgroundColor: Color,
    val onSurfaceColor: Color
)

/**
 * Available themes in the app
 */
object Themes {
    
    // Default Light Theme (Always Available)
    val Default = AppTheme(
        id = "default",
        name = "Classic",
        description = "The original look",
        isLocked = false,
        icon = "☀️",
        primaryColor = Color(0xFF6200EE),
        secondaryColor = Color(0xFF03DAC6),
        tertiaryColor = Color(0xFF018786),
        backgroundColor = Color(0xFFFFFFFF),
        surfaceColor = Color(0xFFF5F5F5),
        errorColor = Color(0xFFB00020),
        onPrimaryColor = Color.White,
        onSecondaryColor = Color.Black,
        onBackgroundColor = Color.Black,
        onSurfaceColor = Color.Black
    )
    
    // Dark Mode (Always Available)
    val Dark = AppTheme(
        id = "dark",
        name = "Dark Mode",
        description = "Easy on the eyes",
        isLocked = false,
        icon = "🌙",
        primaryColor = Color(0xFFBB86FC),
        secondaryColor = Color(0xFF03DAC6),
        tertiaryColor = Color(0xFF3700B3),
        backgroundColor = Color(0xFF121212),
        surfaceColor = Color(0xFF1E1E1E),
        errorColor = Color(0xFFCF6679),
        onPrimaryColor = Color.Black,
        onSecondaryColor = Color.Black,
        onBackgroundColor = Color.White,
        onSurfaceColor = Color.White
    )
    
    // Marvel Theme (Locked)
    val Marvel = AppTheme(
        id = "marvel",
        name = "Marvel Heroes",
        description = "Avengers assemble!",
        isLocked = true,
        unlockRequirement = "Score 300+ points in a single game",
        icon = "🦸‍♂️",
        primaryColor = Color(0xFFED1D24), // Marvel Red
        secondaryColor = Color(0xFFFFD700), // Gold
        tertiaryColor = Color(0xFF0476D9), // Captain America Blue
        backgroundColor = Color(0xFF1A1A1A),
        surfaceColor = Color(0xFF2D2D2D),
        errorColor = Color(0xFFFF4444),
        onPrimaryColor = Color.White,
        onSecondaryColor = Color.Black,
        onBackgroundColor = Color(0xFFEEEEEE),
        onSurfaceColor = Color(0xFFEEEEEE)
    )
    
    // DC Theme (Locked)
    val DC = AppTheme(
        id = "dc",
        name = "DC Universe",
        description = "Justice League unite!",
        isLocked = true,
        unlockRequirement = "Complete 50 games",
        icon = "🦇",
        primaryColor = Color(0xFF0476F2), // Superman Blue
        secondaryColor = Color(0xFFFFD700), // Wonder Woman Gold
        tertiaryColor = Color(0xFF00A650), // Green Lantern
        backgroundColor = Color(0xFF0D1117),
        surfaceColor = Color(0xFF161B22),
        errorColor = Color(0xFFDC143C),
        onPrimaryColor = Color.White,
        onSecondaryColor = Color.Black,
        onBackgroundColor = Color(0xFFF0F0F0),
        onSurfaceColor = Color(0xFFF0F0F0)
    )
    
    // Neon Theme (Locked)
    val Neon = AppTheme(
        id = "neon",
        name = "Neon Nights",
        description = "Cyberpunk vibes",
        isLocked = true,
        unlockRequirement = "Complete 30 games with 3× speed multiplier",
        icon = "⚡",
        primaryColor = Color(0xFFFF006E), // Hot Pink
        secondaryColor = Color(0xFF00F5FF), // Cyan
        tertiaryColor = Color(0xFFFFBE0B), // Yellow
        backgroundColor = Color(0xFF0A0E27),
        surfaceColor = Color(0xFF1A1F3A),
        errorColor = Color(0xFFFF006E),
        onPrimaryColor = Color.White,
        onSecondaryColor = Color.Black,
        onBackgroundColor = Color(0xFFE0E0E0),
        onSurfaceColor = Color(0xFFE0E0E0)
    )
    
    // Ocean Theme (Locked)
    val Ocean = AppTheme(
        id = "ocean",
        name = "Deep Ocean",
        description = "Calm and serene",
        isLocked = true,
        unlockRequirement = "Achieve 7-day streak",
        icon = "🌊",
        primaryColor = Color(0xFF0077BE), // Ocean Blue
        secondaryColor = Color(0xFF00CED1), // Turquoise
        tertiaryColor = Color(0xFF20B2AA), // Light Sea Green
        backgroundColor = Color(0xFF001F3F),
        surfaceColor = Color(0xFF003559),
        errorColor = Color(0xFFFF6B6B),
        onPrimaryColor = Color.White,
        onSecondaryColor = Color.Black,
        onBackgroundColor = Color(0xFFE8F4F8),
        onSurfaceColor = Color(0xFFE8F4F8)
    )
    
    // Sunset Theme (Locked)
    val Sunset = AppTheme(
        id = "sunset",
        name = "Golden Sunset",
        description = "Warm and cozy",
        isLocked = true,
        unlockRequirement = "Earn 5,000 total points",
        icon = "🌅",
        primaryColor = Color(0xFFFF6B35), // Orange
        secondaryColor = Color(0xFFF7931E), // Golden Orange
        tertiaryColor = Color(0xFFFFC300), // Yellow
        backgroundColor = Color(0xFF2D1B00),
        surfaceColor = Color(0xFF3D2B10),
        errorColor = Color(0xFFE74C3C),
        onPrimaryColor = Color.White,
        onSecondaryColor = Color.Black,
        onBackgroundColor = Color(0xFFFFF8E7),
        onSurfaceColor = Color(0xFFFFF8E7)
    )
    
    /**
     * Get all available themes
     */
    fun getAllThemes(): List<AppTheme> {
        return listOf(Default, Dark, Marvel, DC, Neon, Ocean, Sunset)
    }
    
    /**
     * Get unlocked themes based on ThemePreferencesManager
     * Always returns Default and Dark, plus any unlocked themes
     */
    fun getUnlockedThemes(unlockedThemeIds: Set<String>): List<AppTheme> {
        return getAllThemes().filter { theme ->
            !theme.isLocked || unlockedThemeIds.contains(theme.id)
        }
    }
    
    /**
     * Get theme by ID
     */
    fun getThemeById(id: String): AppTheme {
        return getAllThemes().find { it.id == id } ?: Default
    }
}
