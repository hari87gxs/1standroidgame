package com.athreya.mathworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.HighScore
import com.athreya.mathworkout.data.HighScoreDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Data class representing the UI state for the HighScore screen.
 */
data class HighScoreUiState(
    val highScores: List<HighScore> = emptyList(),
    val selectedFilter: HighScoreFilter = HighScoreFilter.ALL,
    val isLoading: Boolean = true
)

/**
 * Enum representing different ways to filter high scores.
 */
enum class HighScoreFilter {
    ALL,
    ADDITION_SUBTRACTION,
    MULTIPLICATION_DIVISION,
    TEST_ME,
    BRAIN_TEASER,
    EASY,
    MEDIUM,
    COMPLEX
}

/**
 * ViewModel for the High Scores screen.
 * 
 * This ViewModel manages the display and filtering of high score data.
 * It loads scores from the Room database and provides filtering capabilities.
 */
class HighScoreViewModel(
    private val highScoreDao: HighScoreDao
) : ViewModel() {
    
    // Private mutable state
    private val _uiState = MutableStateFlow(HighScoreUiState())
    
    // Public read-only state
    val uiState: StateFlow<HighScoreUiState> = _uiState.asStateFlow()
    
    // Keep a reference to all scores for filtering
    private var allScores: List<HighScore> = emptyList()
    
    init {
        // Load high scores when ViewModel is created
        loadHighScores()
    }
    
    /**
     * Load all high scores from the database.
     */
    private fun loadHighScores() {
        viewModelScope.launch {
            // Collect from the Room DAO's Flow
            // This will automatically update when the database changes
            highScoreDao.getAllHighScores().collect { scores ->
                allScores = scores
                applyCurrentFilter()
            }
        }
    }
    
    /**
     * Apply the currently selected filter to the high scores.
     */
    private fun applyCurrentFilter() {
        val currentState = _uiState.value
        val filteredScores = when (currentState.selectedFilter) {
            HighScoreFilter.ALL -> allScores
            HighScoreFilter.ADDITION_SUBTRACTION -> allScores.filter { 
                it.gameMode == GameMode.ADDITION_SUBTRACTION.name 
            }
            HighScoreFilter.MULTIPLICATION_DIVISION -> allScores.filter { 
                it.gameMode == GameMode.MULTIPLICATION_DIVISION.name 
            }
            HighScoreFilter.TEST_ME -> allScores.filter { 
                it.gameMode == GameMode.TEST_ME.name 
            }
            HighScoreFilter.BRAIN_TEASER -> allScores.filter { 
                it.gameMode == GameMode.BRAIN_TEASER.name 
            }
            HighScoreFilter.EASY -> allScores.filter { it.difficulty == "EASY" }
            HighScoreFilter.MEDIUM -> allScores.filter { it.difficulty == "MEDIUM" }
            HighScoreFilter.COMPLEX -> allScores.filter { it.difficulty == "COMPLEX" }
        }
        
        _uiState.value = currentState.copy(
            highScores = filteredScores,
            isLoading = false
        )
    }
    
    /**
     * Change the filter for displaying high scores.
     * 
     * @param filter The new filter to apply
     */
    fun setFilter(filter: HighScoreFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyCurrentFilter()
    }
    
    /**
     * Save a new high score to the database.
     * This should be called from the Results screen when a game is completed.
     * 
     * @param gameMode The game mode that was played
     * @param difficulty The difficulty level that was played
     * @param timeTaken The total time including penalties
     * @param wrongAttempts The number of wrong attempts
     */
    fun saveHighScore(
        gameMode: GameMode,
        difficulty: String,
        timeTaken: Long,
        wrongAttempts: Int
    ) {
        viewModelScope.launch {
            val highScore = HighScore(
                gameMode = gameMode.name,
                difficulty = difficulty,
                timeTaken = timeTaken,
                wrongAttempts = wrongAttempts
            )
            
            highScoreDao.insertHighScore(highScore)
            // The UI will update automatically through the Flow collection
        }
    }
    
    /**
     * Check if a given time would be a new record for the specified mode and difficulty.
     * 
     * @param gameMode The game mode to check
     * @param difficulty The difficulty to check
     * @param timeTaken The time to check
     * @return True if this would be a new record (best time)
     */
    suspend fun isNewRecord(gameMode: GameMode, difficulty: String, timeTaken: Long): Boolean {
        val bestTime = highScoreDao.getBestTime(gameMode.name, difficulty)
        return bestTime == null || timeTaken < bestTime
    }
    
    /**
     * Format a time in milliseconds to a human-readable string.
     * 
     * @param timeMs Time in milliseconds
     * @return Formatted string (e.g., "1m 23s", "45s")
     */
    fun formatTime(timeMs: Long): String {
        val seconds = timeMs / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        
        return if (minutes > 0) {
            "${minutes}m ${remainingSeconds}s"
        } else {
            "${remainingSeconds}s"
        }
    }
}