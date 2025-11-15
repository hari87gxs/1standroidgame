package com.athreya.mathworkout.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.HighScore
import com.athreya.mathworkout.data.HighScoreDao
import com.athreya.mathworkout.data.ScoreRepositoryImpl
import com.athreya.mathworkout.data.UserPreferencesManager
import kotlinx.coroutines.flow.first
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
    private val highScoreDao: HighScoreDao,
    private val context: Context
) : ViewModel() {
    
    private val userPreferences = UserPreferencesManager(context)
    private val scoreRepository = ScoreRepositoryImpl(context)
    
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
            // Validate score - don't save if time is 0 or negative (game not completed properly)
            if (timeTaken <= 0) {
                return@launch
            }
            
            val playerName = userPreferences.getPlayerName() ?: "Anonymous"
            val highScore = HighScore(
                playerName = playerName,
                deviceId = userPreferences.getDeviceId(),
                gameMode = gameMode.name,
                difficulty = difficulty,
                timeTaken = timeTaken,
                wrongAttempts = wrongAttempts
            )
            
            // Use the repository to save score locally and sync to Firebase automatically
            scoreRepository.insertHighScore(highScore, syncToCloud = true)
            // The UI will update automatically through the Flow collection
        }
    }
    
    /**
     * Check if a given score would be a new record for the specified mode and difficulty.
     * 
     * @param gameMode The game mode to check
     * @param difficulty The difficulty to check
     * @param score The score to check
     * @return True if this would be a new record (best score)
     */
    suspend fun isNewRecord(gameMode: GameMode, difficulty: String, score: Int): Boolean {
        return try {
            val bestScore = highScoreDao.getBestScore(gameMode.name, difficulty)
            bestScore == null || score > bestScore
        } catch (e: Exception) {
            e.printStackTrace()
            false // Default to not a record on error
        }
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
    
    /**
     * Save a high score with points calculation (enhanced version)
     */
    fun saveHighScoreWithPoints(
        gameMode: GameMode,
        difficulty: String,
        timeTaken: Long,
        wrongAttempts: Int,
        questionsAnswered: Int = 10,
        isDailyChallenge: Boolean = false,
        bonusMultiplier: Float = 1.0f
    ) {
        viewModelScope.launch {
            if (timeTaken <= 0) return@launch
            
            val playerName = userPreferences.getPlayerName() ?: "Anonymous"
            val correctAnswers = questionsAnswered - wrongAttempts
            val basePoints = HighScore.calculatePoints(
                questionsAnswered, correctAnswers, wrongAttempts, timeTaken, difficulty
            )
            val finalScore = HighScore.applyBonus(basePoints, bonusMultiplier)
            
            val highScore = HighScore(
                playerName = playerName,
                deviceId = userPreferences.getDeviceId(),
                gameMode = gameMode.name,
                difficulty = difficulty,
                timeTaken = timeTaken,
                wrongAttempts = wrongAttempts,
                points = basePoints,
                bonusMultiplier = bonusMultiplier,
                finalScore = finalScore,
                isDailyChallenge = isDailyChallenge
            )
            
            scoreRepository.insertHighScore(highScore, syncToCloud = true)
        }
    }
    
    /**
     * Get average score for a specific game mode and difficulty
     */
    suspend fun getAverageScore(gameMode: GameMode, difficulty: String): Int {
        return try {
            val scores = scoreRepository.getLocalHighScores().first()
            val filteredScores = scores.filter { 
                it.gameMode == gameMode.name && it.difficulty == difficulty 
            }
            if (filteredScores.isEmpty()) 0
            else filteredScores.map { it.finalScore }.average().toInt()
        } catch (e: Exception) {
            0
        }
    }
    
    /**
     * Get average time for a specific game mode and difficulty
     */
    suspend fun getAverageTime(gameMode: GameMode, difficulty: String): Float {
        return try {
            val scores = scoreRepository.getLocalHighScores().first()
            val filteredScores = scores.filter { 
                it.gameMode == gameMode.name && it.difficulty == difficulty 
            }
            if (filteredScores.isEmpty()) 0f
            else filteredScores.map { it.timeTaken }.average().toFloat() / 1000f
        } catch (e: Exception) {
            0f
        }
    }
    
    /**
     * Get best time for a specific game mode and difficulty
     */
    suspend fun getBestTime(gameMode: GameMode, difficulty: String): Float {
        return try {
            val scores = scoreRepository.getLocalHighScores().first()
            val filteredScores = scores.filter { 
                it.gameMode == gameMode.name && it.difficulty == difficulty 
            }
            if (filteredScores.isEmpty()) Float.MAX_VALUE
            else filteredScores.minOf { it.timeTaken }.toFloat() / 1000f
        } catch (e: Exception) {
            Float.MAX_VALUE
        }
    }
    
    /**
     * Get best score for a specific game mode and difficulty
     */
    suspend fun getBestScoreValue(gameMode: GameMode, difficulty: String): Int {
        return try {
            val scores = scoreRepository.getLocalHighScores().first()
            val filteredScores = scores.filter { 
                it.gameMode == gameMode.name && it.difficulty == difficulty 
            }
            if (filteredScores.isEmpty()) 0
            else filteredScores.maxOf { it.finalScore }
        } catch (e: Exception) {
            0
        }
    }
}
