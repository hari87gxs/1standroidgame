package com.athreya.mathworkout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.HighScore
import com.athreya.mathworkout.data.ScoreRepository
import com.athreya.mathworkout.data.SyncStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
/**
 * ViewModel for managing global leaderboard state and operations.
 * Handles loading global scores, player rankings, and synchronization status.
 * 
 * @param scoreRepository Repository for score data operations
 */
class GlobalLeaderboardViewModel(
    private val scoreRepository: ScoreRepository
) : ViewModel() {
    
    // UI State for Global Leaderboard
    private val _uiState = MutableStateFlow(GlobalLeaderboardUiState())
    val uiState: StateFlow<GlobalLeaderboardUiState> = _uiState.asStateFlow()
    
    // Sync Status
    private val _syncStatus = MutableStateFlow<SyncStatus?>(null)
    val syncStatus: StateFlow<SyncStatus?> = _syncStatus.asStateFlow()
    
    init {
        loadGlobalLeaderboard()
        loadSyncStatus()
    }
    
    /**
     * Load global leaderboard with current filters
     */
    fun loadGlobalLeaderboard() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                val gameMode = currentState.selectedGameMode?.name ?: "ALL"
                val difficulty = currentState.selectedDifficulty?.name ?: "All Levels"
                
                val result = scoreRepository.getGlobalLeaderboard(
                    limit = 100,
                    gameMode = if (gameMode == "ALL") null else gameMode,
                    difficulty = if (difficulty == "All Levels") null else difficulty
                )
                
                if (result.isSuccess) {
                    val scores = result.getOrThrow()
                    _uiState.value = currentState.copy(
                        globalScores = scores,
                        isLoading = false,
                        error = null
                    )
                    
                    // Load player rank if we have scores
                    if (scores.isNotEmpty()) {
                        loadPlayerRank()
                    }
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to load leaderboard"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    /**
     * Load player's current rank on the global leaderboard
     */
    private fun loadPlayerRank() {
        viewModelScope.launch {
            try {
                val deviceId = (scoreRepository as? com.athreya.mathworkout.data.ScoreRepositoryImpl)?.getDeviceId() ?: return@launch
                val currentState = _uiState.value
                
                val gameMode = currentState.selectedGameMode?.name ?: "ALL"
                val difficulty = currentState.selectedDifficulty?.name ?: "All Levels"
                
                val result = scoreRepository.getPlayerGlobalRank(
                    deviceId = deviceId,
                    gameMode = if (gameMode == "ALL") null else gameMode,
                    difficulty = if (difficulty == "All Levels") null else difficulty
                )
                
                if (result.isSuccess) {
                    _uiState.value = currentState.copy(playerRank = result.getOrThrow())
                }
            } catch (e: Exception) {
                // Silently fail for rank loading - not critical for UI
            }
        }
    }
    
    /**
     * Filter leaderboard by game mode
     */
    fun filterByGameMode(gameMode: GameMode?) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(selectedGameMode = gameMode)
        loadGlobalLeaderboard()
    }
    
    /**
     * Filter leaderboard by difficulty
     */
    fun filterByDifficulty(difficulty: Difficulty?) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(selectedDifficulty = difficulty)
        loadGlobalLeaderboard()
    }
    
    /**
     * Refresh leaderboard data
     */
    fun refreshLeaderboard() {
        loadGlobalLeaderboard()
        loadSyncStatus()
    }
    
    /**
     * Sync unsynced local scores to global leaderboard
     */
    fun syncLocalScores() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            
            try {
                val result = scoreRepository.syncUnsyncedScores()
                if (result.isSuccess) {
                    val syncedCount = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        lastSyncMessage = "Synced $syncedCount scores successfully"
                    )
                    
                    // Refresh leaderboard after sync
                    loadGlobalLeaderboard()
                    loadSyncStatus()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSyncing = false,
                        error = "Sync failed: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSyncing = false,
                    error = "Sync failed: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Load current sync status
     */
    private fun loadSyncStatus() {
        viewModelScope.launch {
            try {
                val status = scoreRepository.getSyncStatus()
                _syncStatus.value = status
            } catch (e: Exception) {
                // Silently fail - sync status is not critical
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Clear last sync message
     */
    fun clearSyncMessage() {
        _uiState.value = _uiState.value.copy(lastSyncMessage = null)
    }
    
    /**
     * Check if device is online
     */
    fun checkOnlineStatus() {
        viewModelScope.launch {
            val isOnline = scoreRepository.isOnline()
            _uiState.value = _uiState.value.copy(isOnline = isOnline)
        }
    }
    
    /**
     * Clear all global leaderboard data (for development/testing)
     * WARNING: This will delete ALL scores from Firebase
     */
    fun clearGlobalLeaderboard() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                val result = scoreRepository.clearGlobalLeaderboard()
                if (result.isSuccess) {
                    val deletedCount = result.getOrThrow()
                    _uiState.value = currentState.copy(
                        globalScores = emptyList(),
                        isLoading = false,
                        lastSyncMessage = "Cleared $deletedCount scores from global leaderboard",
                        error = null
                    )
                } else {
                    _uiState.value = currentState.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to clear leaderboard"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    /**
     * Clear local duplicate scores (for development/testing)
     * This will remove scores created with "Player" name
     */
    fun clearLocalDuplicates() {
        viewModelScope.launch {
            try {
                val repository = scoreRepository as? com.athreya.mathworkout.data.ScoreRepositoryImpl
                val deletedCount = repository?.deleteLocalScoresByPlayerName("Player") ?: 0
                _uiState.value = _uiState.value.copy(
                    lastSyncMessage = "Removed $deletedCount duplicate local scores"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to clear local duplicates"
                )
            }
        }
    }
}

/**
 * UI State for Global Leaderboard screen
 */
data class GlobalLeaderboardUiState(
    val globalScores: List<HighScore> = emptyList(),
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val selectedGameMode: GameMode? = null,
    val selectedDifficulty: Difficulty? = null,
    val playerRank: Int? = null,
    val isOnline: Boolean = true,
    val lastSyncMessage: String? = null
)