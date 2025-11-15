package com.athreya.mathworkout.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.*
import com.athreya.mathworkout.data.repository.FeaturesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for Daily Challenge Screen
 */
class DailyChallengeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val featuresRepository = FeaturesRepository(
        dailyChallengeDao = database.dailyChallengeDao(),
        achievementDao = database.achievementDao(),
        dailyStreakDao = database.dailyStreakDao(),
        timedChallengeDao = database.timedChallengeDao(),
        multiplayerGameDao = database.multiplayerGameDao(),
        highScoreDao = database.highScoreDao()
    )
    
    // UI State
    private val _uiState = MutableStateFlow(DailyChallengeUiState())
    val uiState: StateFlow<DailyChallengeUiState> = _uiState.asStateFlow()
    
    init {
        try {
            loadTodaysChallenge()
            loadCompletedChallenges()
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }
    
    /**
     * Load or generate today's challenge
     */
    private fun loadTodaysChallenge() {
        viewModelScope.launch {
            try {
                val challenge = featuresRepository.getTodaysChallenge()
                _uiState.update { it.copy(
                    todaysChallenge = challenge,
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message,
                    isLoading = false
                )}
            }
        }
    }
    
    /**
     * Load history of completed challenges
     */
    private fun loadCompletedChallenges() {
        viewModelScope.launch {
            featuresRepository.getCompletedChallenges()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { challenges ->
                    _uiState.update { it.copy(completedChallenges = challenges) }
                }
        }
    }
    
    /**
     * Mark today's challenge as complete
     */
    fun completeChallenge(timeTaken: Long, wrongAttempts: Int) {
        viewModelScope.launch {
            try {
                featuresRepository.completeDailyChallenge(timeTaken, wrongAttempts)
                // Reload to get updated challenge
                loadTodaysChallenge()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    /**
     * Complete daily challenge with points calculation
     */
    suspend fun completeDailyChallenge(timeTaken: Long, wrongAttempts: Int, questionsAnswered: Int) {
        try {
            featuresRepository.completeDailyChallenge(timeTaken, wrongAttempts, questionsAnswered)
            loadTodaysChallenge()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Get current streak multiplier
     */
    suspend fun getStreakMultiplier(): Float {
        return try {
            featuresRepository.getStreakMultiplier()
        } catch (e: Exception) {
            e.printStackTrace()
            1.0f // Default multiplier on error
        }
    }
    
    /**
     * Get current streak days
     */
    suspend fun getCurrentStreak(): Int {
        return try {
            featuresRepository.getCurrentStreak().currentStreak
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    /**
     * Refresh challenge data
     */
    fun refresh() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        loadTodaysChallenge()
        loadCompletedChallenges()
    }
}

/**
 * UI State for Daily Challenge Screen
 */
data class DailyChallengeUiState(
    val todaysChallenge: DailyChallenge? = null,
    val completedChallenges: List<DailyChallenge> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)
