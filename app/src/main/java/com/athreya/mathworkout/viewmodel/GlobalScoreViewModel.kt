package com.athreya.mathworkout.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.network.*
import com.athreya.mathworkout.data.repository.GlobalScoreRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * UI state for the Global Score screen.
 */
data class GlobalScoreUiState(
    val isUserRegistered: Boolean = false,
    val userName: String = "",
    val selectedGameMode: String = "ALL",
    val weeklyLeaderboard: WeeklyLeaderboard? = null,
    val userScores: List<GlobalScore> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showRegistrationDialog: Boolean = false,
    val availableGameModes: List<String> = listOf("ALL", "ADDITION_SUBTRACTION", "MULTIPLICATION_DIVISION", "TEST_ME", "BRAIN_TEASER", "SUDOKU")
)

/**
 * ViewModel for managing global scores and leaderboard functionality.
 */
class GlobalScoreViewModel(context: Context) : ViewModel() {
    
    private val repository = GlobalScoreRepository(context)
    
    private val _uiState = MutableStateFlow(GlobalScoreUiState())
    val uiState: StateFlow<GlobalScoreUiState> = _uiState.asStateFlow()
    
    init {
        checkUserRegistration()
        loadWeeklyLeaderboard()
    }
    
    /**
     * Check if user is registered and load their data.
     */
    private fun checkUserRegistration() {
        viewModelScope.launch {
            try {
                val isRegistered = repository.isUserRegistered()
                val userName = repository.getUserName() ?: ""
                
                _uiState.value = _uiState.value.copy(
                    isUserRegistered = isRegistered,
                    userName = userName,
                    showRegistrationDialog = !isRegistered
                )
                
                if (isRegistered) {
                    loadUserScores()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to check user registration: ${e.localizedMessage}"
                )
            }
        }
    }
    
    /**
     * Register a new user.
     */
    fun registerUser(userName: String) {
        viewModelScope.launch {
            repository.registerUser(userName).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            errorMessage = null
                        )
                    }
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isUserRegistered = true,
                            userName = userName,
                            showRegistrationDialog = false,
                            successMessage = "Welcome to Global Scores, $userName!"
                        )
                        loadUserScores()
                        loadWeeklyLeaderboard()
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Submit a score to the global leaderboard.
     */
    fun submitScore(
        gameMode: GameMode,
        difficulty: String?,
        wrongAttempts: Int,
        timeInMillis: Long
    ) {
        viewModelScope.launch {
            val score = ScoreUtils.calculateScore(timeInMillis, wrongAttempts, difficulty ?: "EASY")
            
            repository.submitScore(
                gameMode = gameMode.name,
                difficulty = difficulty,
                score = score,
                timeInMillis = timeInMillis
            ).collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            successMessage = "Score submitted successfully! Score: ${result.data.score}"
                        )
                        // Refresh leaderboard and user scores
                        loadWeeklyLeaderboard()
                        loadUserScores()
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Load the weekly leaderboard.
     */
    fun loadWeeklyLeaderboard() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val gameMode = if (currentState.selectedGameMode == "ALL") null else currentState.selectedGameMode
            
            val flow = if (gameMode == null) {
                repository.getWeeklyLeaderboard()
            } else {
                repository.getWeeklyLeaderboardByGameMode(gameMode)
            }
            
            flow.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                    }
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            weeklyLeaderboard = result.data
                        )
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Load user's personal scores.
     */
    private fun loadUserScores() {
        viewModelScope.launch {
            repository.getUserScores().collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(userScores = result.data)
                    }
                    is NetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to load your scores: ${result.message}"
                        )
                    }
                    is NetworkResult.Loading -> {
                        // Don't show loading for background user score loading
                    }
                }
            }
        }
    }
    
    /**
     * Change the selected game mode filter.
     */
    fun selectGameMode(gameMode: String) {
        _uiState.value = _uiState.value.copy(selectedGameMode = gameMode)
        loadWeeklyLeaderboard()
    }
    
    /**
     * Refresh all data.
     */
    fun refresh() {
        loadWeeklyLeaderboard()
        if (_uiState.value.isUserRegistered) {
            loadUserScores()
        }
    }
    
    /**
     * Clear error message.
     */
    fun clearErrorMessage() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    /**
     * Clear success message.
     */
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
    
    /**
     * Show registration dialog.
     */
    fun showRegistrationDialog() {
        _uiState.value = _uiState.value.copy(showRegistrationDialog = true)
    }
    
    /**
     * Hide registration dialog.
     */
    fun hideRegistrationDialog() {
        _uiState.value = _uiState.value.copy(showRegistrationDialog = false)
    }
    
    /**
     * Get formatted game mode display name.
     */
    fun getGameModeDisplayName(gameMode: String): String {
        return when (gameMode) {
            "ALL" -> "All Games"
            "ADDITION_SUBTRACTION" -> "Addition & Subtraction"
            "MULTIPLICATION_DIVISION" -> "Multiplication & Division"
            "TEST_ME" -> "Test Me"
            "BRAIN_TEASER" -> "Brain Teaser"
            "SUDOKU" -> "Sudoku"
            else -> gameMode
        }
    }
    
    /**
     * Get the user's rank in the current leaderboard.
     */
    fun getCurrentUserRank(): Int? {
        return _uiState.value.weeklyLeaderboard?.entries?.find { it.isCurrentUser }?.rank
    }
    
    /**
     * Get the user's best time for the current game mode.
     */
    fun getUserBestTime(gameMode: String): Long? {
        return _uiState.value.userScores
            .filter { it.gameMode == gameMode || gameMode == "ALL" }
            .minByOrNull { it.timeInMillis }?.timeInMillis
    }
    
    /**
     * Clear all local data (for testing).
     */
    fun clearLocalData() {
        viewModelScope.launch {
            repository.clearUserData()
            _uiState.value = GlobalScoreUiState()
            checkUserRegistration()
        }
    }
}