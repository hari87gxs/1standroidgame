package com.athreya.mathworkout.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages theme preferences
 */
class ThemePreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "theme_preferences",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_CURRENT_THEME = "current_theme"
        private const val KEY_UNLOCKED_THEMES = "unlocked_themes"
        private const val DEFAULT_THEME_ID = "default"
    }
    
    /**
     * Get current theme ID
     */
    fun getCurrentThemeId(): String {
        return prefs.getString(KEY_CURRENT_THEME, DEFAULT_THEME_ID) ?: DEFAULT_THEME_ID
    }
    
    /**
     * Set current theme
     */
    fun setCurrentTheme(themeId: String) {
        prefs.edit().putString(KEY_CURRENT_THEME, themeId).apply()
    }
    
    /**
     * Get unlocked theme IDs
     */
    fun getUnlockedThemes(): Set<String> {
        // Default and Dark are always unlocked
        val defaultUnlocked = setOf("default", "dark")
        val saved = prefs.getStringSet(KEY_UNLOCKED_THEMES, emptySet()) ?: emptySet()
        return defaultUnlocked + saved
    }
    
    /**
     * Unlock a theme
     */
    fun unlockTheme(themeId: String) {
        val current = getUnlockedThemes().toMutableSet()
        current.add(themeId)
        prefs.edit().putStringSet(KEY_UNLOCKED_THEMES, current).apply()
    }
    
    /**
     * Check if a theme is unlocked
     */
    fun isThemeUnlocked(themeId: String): Boolean {
        return getUnlockedThemes().contains(themeId)
    }
    
    /**
     * Unlock all themes (for testing)
     */
    fun unlockAllThemes() {
        val allThemes = setOf("default", "dark", "marvel", "dc", "neon", "ocean", "sunset")
        prefs.edit().putStringSet(KEY_UNLOCKED_THEMES, allThemes).apply()
    }
    
    /**
     * Reset to default theme
     */
    fun resetTheme() {
        prefs.edit()
            .remove(KEY_CURRENT_THEME)
            .remove(KEY_UNLOCKED_THEMES)
            .apply()
    }
}
