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
}