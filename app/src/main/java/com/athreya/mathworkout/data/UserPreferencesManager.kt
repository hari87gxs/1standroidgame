package com.athreya.mathworkout.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages user preferences including player name and registration status
 */
class UserPreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_preferences", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_PLAYER_NAME = "player_name"
        private const val KEY_IS_REGISTERED = "is_registered"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_TOTAL_XP = "total_xp"
        private const val KEY_SELECTED_AVATAR = "selected_avatar"
    }
    
    /**
     * Get the current player name
     */
    fun getPlayerName(): String? {
        return prefs.getString(KEY_PLAYER_NAME, null)
    }
    
    /**
     * Set the player name
     */
    fun setPlayerName(name: String) {
        prefs.edit()
            .putString(KEY_PLAYER_NAME, name)
            .putBoolean(KEY_IS_REGISTERED, true)
            .apply()
    }
    
    /**
     * Check if user is registered (has set a player name)
     */
    fun isUserRegistered(): Boolean {
        return prefs.getBoolean(KEY_IS_REGISTERED, false) && getPlayerName() != null
    }
    
    /**
     * Clear user registration
     */
    fun clearRegistration() {
        prefs.edit()
            .remove(KEY_PLAYER_NAME)
            .putBoolean(KEY_IS_REGISTERED, false)
            .apply()
    }
    
    /**
     * Get or generate device ID
     */
    fun getDeviceId(): String {
        val existing = prefs.getString(KEY_DEVICE_ID, null)
        if (existing != null) {
            return existing
        }
        
        // Generate new device ID
        val newId = java.util.UUID.randomUUID().toString()
        prefs.edit().putString(KEY_DEVICE_ID, newId).apply()
        return newId
    }
    
    /**
     * Get total XP points
     */
    fun getTotalXP(): Int {
        return prefs.getInt(KEY_TOTAL_XP, 0)
    }
    
    /**
     * Set total XP points
     */
    fun setTotalXP(xp: Int) {
        prefs.edit().putInt(KEY_TOTAL_XP, xp).apply()
    }
    
    /**
     * Add XP points
     */
    fun addXP(xp: Int) {
        val current = getTotalXP()
        setTotalXP(current + xp)
    }
    
    /**
     * Get selected avatar ID
     */
    fun getSelectedAvatar(): String {
        return prefs.getString(KEY_SELECTED_AVATAR, "default") ?: "default"
    }
    
    /**
     * Set selected avatar ID
     */
    fun setSelectedAvatar(avatarId: String) {
        prefs.edit().putString(KEY_SELECTED_AVATAR, avatarId).apply()
    }
    
    // Generic preference methods for use by other managers
    fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }
    
    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
    
    fun getInt(key: String, defaultValue: Int): Int {
        return prefs.getInt(key, defaultValue)
    }
    
    fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }
    
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }
    
    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
