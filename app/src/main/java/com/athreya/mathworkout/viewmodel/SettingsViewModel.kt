package com.athreya.mathworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.GameSettings
import com.athreya.mathworkout.data.SettingsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * 
 * ViewModels are part of Android's Architecture Components and serve as the bridge
 * between the UI (Composables) and the data layer. They:
 * - Survive configuration changes (like screen rotation)
 * - Handle business logic
 * - Manage UI state
 * - Provide data to the UI via StateFlow/LiveData
 * 
 * This ViewModel manages the user's game settings and provides functions
 * to update them.
 */
class SettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {
    
    // Private mutable state for current settings
    private val _uiState = MutableStateFlow(GameSettings())
    
    /**
     * Public read-only StateFlow that the UI observes.
     * StateFlow is a reactive data holder that emits updates to its collectors.
     * It's perfect for representing UI state in Compose.
     */
    val uiState: StateFlow<GameSettings> = _uiState.asStateFlow()
    
    init {
        // Load initial settings when ViewModel is created
        loadSettings()
    }
    
    /**
     * Load current settings from DataStore.
     * This runs in the ViewModel's scope, which is tied to the ViewModel's lifecycle.
     */
    private fun loadSettings() {
        viewModelScope.launch {
            // Collect from the settings manager's Flow
            settingsManager.gameSettings.collect { settings ->
                _uiState.value = settings
            }
        }
    }
    
    /**
     * Update the difficulty setting.
     * 
     * @param difficulty The new difficulty level
     */
    fun updateDifficulty(difficulty: Difficulty) {
        viewModelScope.launch {
            // Update in DataStore
            settingsManager.updateDifficulty(difficulty)
            // Note: UI state will update automatically through the Flow collection
        }
    }
    
    /**
     * Update the question count setting.
     * 
     * @param count The new question count
     */
    fun updateQuestionCount(count: Int) {
        viewModelScope.launch {
            settingsManager.updateQuestionCount(count)
        }
    }
    
    /**
     * Get available question count options.
     * These are the standard options presented to the user.
     */
    fun getQuestionCountOptions(): List<Int> {
        return listOf(10, 20, 50)
    }
    
    /**
     * Get available difficulty options.
     */
    fun getDifficultyOptions(): List<Difficulty> {
        return Difficulty.values().toList()
    }
}