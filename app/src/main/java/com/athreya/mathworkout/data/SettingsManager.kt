package com.athreya.mathworkout.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * SettingsManager handles saving and retrieving user preferences using Jetpack DataStore.
 *
 * DataStore is the modern replacement for SharedPreferences. It's:
 * - Type-safe (with Preferences DataStore)
 * - Asynchronous (uses Kotlin coroutines)
 * - Handles errors gracefully
 * - Thread-safe
 *
 * This class manages the user's game settings like difficulty and question count.
 */
class SettingsManager(private val context: Context) {
    
    companion object {
        // Extension property to get DataStore instance
        // This creates a singleton DataStore instance per process
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = "game_settings"
        )
        
        // Keys for storing preferences
        // These are type-safe keys that define what type of data we're storing
        private val DIFFICULTY_KEY = stringPreferencesKey("difficulty")
        private val QUESTION_COUNT_KEY = intPreferencesKey("question_count")
    }
    
    /**
     * Get the current game settings as a Flow.
     *
     * Flow is a reactive stream that emits new values whenever the settings change.
     * This allows the UI to automatically update when settings are modified.
     *
     * @return Flow<GameSettings> that emits current settings
     */
    val gameSettings: Flow<GameSettings> = context.dataStore.data.map { preferences ->
        // Extract difficulty from preferences, default to EASY
        val difficultyString = preferences[DIFFICULTY_KEY] ?: Difficulty.EASY.name
        val difficulty = try {
            Difficulty.valueOf(difficultyString)
        } catch (e: IllegalArgumentException) {
            // If stored value is invalid, use default
            Difficulty.EASY
        }
        
        // Extract question count, default to 10
        val questionCount = preferences[QUESTION_COUNT_KEY] ?: 10
        
        // Return the complete settings object
        GameSettings(
            difficulty = difficulty,
            questionCount = questionCount
        )
    }
    
    /**
     * Update the difficulty setting.
     *
     * This function is suspend because DataStore operations are asynchronous.
     * The 'edit' function allows us to modify preferences atomically.
     *
     * @param difficulty The new difficulty level to save
     */
    suspend fun updateDifficulty(difficulty: Difficulty) {
        context.dataStore.edit { preferences ->
            preferences[DIFFICULTY_KEY] = difficulty.name
        }
    }
    
    /**
     * Update the question count setting.
     *
     * @param count The new question count to save
     */
    suspend fun updateQuestionCount(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[QUESTION_COUNT_KEY] = count
        }
    }
    
    /**
     * Update both settings at once.
     * This is more efficient than calling update functions separately.
     *
     * @param settings The complete settings object to save
     */
    suspend fun updateSettings(settings: GameSettings) {
        context.dataStore.edit { preferences ->
            preferences[DIFFICULTY_KEY] = settings.difficulty.name
            preferences[QUESTION_COUNT_KEY] = settings.questionCount
        }
    }
}
