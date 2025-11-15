package com.athreya.mathworkout.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.AppDatabase
import com.athreya.mathworkout.data.Difficulty
import com.athreya.mathworkout.data.GameMode
import com.athreya.mathworkout.data.UserPreferencesManager
import com.athreya.mathworkout.data.repository.ChallengeRepository
import com.athreya.mathworkout.data.social.Challenge
import com.athreya.mathworkout.data.social.ChallengeFirebaseService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing challenges
 */
class ChallengeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val challengeDao = database.challengeDao()
    private val groupMemberDao = database.groupMemberDao()
    private val userPreferences = UserPreferencesManager(application)
    
    private val firebaseService = ChallengeFirebaseService(
        FirebaseFirestore.getInstance(),
        application
    )
    
    private val repository = ChallengeRepository(
        challengeDao,
        groupMemberDao,
        userPreferences,
        firebaseService
    )
    
    // UI State
    private val _uiState = MutableStateFlow<ChallengeUiState>(ChallengeUiState.Loading)
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()
    
    private val _myChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val myChallenges: StateFlow<List<Challenge>> = _myChallenges.asStateFlow()
    
    private val _pendingChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val pendingChallenges: StateFlow<List<Challenge>> = _pendingChallenges.asStateFlow()
    
    private val _activeChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val activeChallenges: StateFlow<List<Challenge>> = _activeChallenges.asStateFlow()
    
    private val _completedChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val completedChallenges: StateFlow<List<Challenge>> = _completedChallenges.asStateFlow()
    
    private val _groupChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val groupChallenges: StateFlow<List<Challenge>> = _groupChallenges.asStateFlow()
    
    private val _selectedChallenge = MutableStateFlow<Challenge?>(null)
    val selectedChallenge: StateFlow<Challenge?> = _selectedChallenge.asStateFlow()
    
    private val _pendingChallengeCount = MutableStateFlow(0)
    val pendingChallengeCount: StateFlow<Int> = _pendingChallengeCount.asStateFlow()
    
    init {
        syncChallengesFromFirebase()
        loadMyChallenges()
        loadPendingChallenges()
        loadActiveChallenges()
        loadCompletedChallenges()
        loadPendingCount()
    }
    
    /**
     * Sync challenges from Firebase
     */
    fun syncChallengesFromFirebase() {
        viewModelScope.launch {
            android.util.Log.d("ChallengeViewModel", "Syncing challenges from Firebase...")
            val result = repository.syncChallengesFromFirebase()
            if (result.isSuccess) {
                android.util.Log.d("ChallengeViewModel", "Challenges synced successfully")
            } else {
                android.util.Log.e("ChallengeViewModel", "Failed to sync challenges: ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    /**
     * Load all user's challenges
     */
    private fun loadMyChallenges() {
        viewModelScope.launch {
            repository.getMyChallenges().collect { challenges ->
                _myChallenges.value = challenges
            }
        }
    }
    
    /**
     * Load pending challenges
     */
    private fun loadPendingChallenges() {
        viewModelScope.launch {
            repository.getPendingChallenges().collect { challenges ->
                _pendingChallenges.value = challenges
            }
        }
    }
    
    /**
     * Load active challenges
     */
    private fun loadActiveChallenges() {
        viewModelScope.launch {
            repository.getActiveChallenges().collect { challenges ->
                _activeChallenges.value = challenges
            }
        }
    }
    
    /**
     * Load completed challenges (completed, declined, expired, cancelled)
     */
    private fun loadCompletedChallenges() {
        viewModelScope.launch {
            repository.getCompletedChallenges().collect { challenges ->
                _completedChallenges.value = challenges
            }
        }
    }
    
    /**
     * Load pending challenge count for badge
     */
    private fun loadPendingCount() {
        viewModelScope.launch {
            repository.getPendingChallengeCount().collect { count ->
                _pendingChallengeCount.value = count
            }
        }
    }
    
    /**
     * Load challenges for a specific group
     */
    fun loadGroupChallenges(groupId: String) {
        viewModelScope.launch {
            repository.getGroupChallenges(groupId).collect { challenges ->
                _groupChallenges.value = challenges
            }
        }
    }
    
    /**
     * Load a specific challenge
     */
    fun loadChallenge(challengeId: String) {
        viewModelScope.launch {
            repository.getChallenge(challengeId).collect { challenge ->
                _selectedChallenge.value = challenge
            }
        }
    }
    
    /**
     * Get a challenge synchronously from current state
     * Used for navigation when clicking on a challenge
     */
    fun getChallenge(challengeId: String): Challenge? {
        // Check all loaded challenges
        val allChallenges = _pendingChallenges.value + _activeChallenges.value + _completedChallenges.value
        return allChallenges.firstOrNull { it.challengeId == challengeId }
    }
    
    /**
     * Create a challenge to a specific member
     */
    fun createChallenge(
        groupId: String,
        challengedId: String,
        challengedName: String,
        gameMode: GameMode,
        difficulty: Difficulty,
        questionCount: Int = 10,
        onSuccess: (Challenge) -> Unit,
        onError: (String) -> Unit
    ) {
        android.util.Log.d("ChallengeViewModel", "createChallenge called: groupId=$groupId, challengedId=$challengedId")
        viewModelScope.launch {
            _uiState.value = ChallengeUiState.Loading
            
            val result = repository.createChallenge(
                groupId,
                challengedId,
                challengedName,
                gameMode,
                difficulty,
                questionCount
            )
            
            if (result.isSuccess) {
                val challenge = result.getOrNull()!!
                android.util.Log.d("ChallengeViewModel", "Challenge created successfully: ${challenge.challengeId}")
                _uiState.value = ChallengeUiState.Success
                onSuccess(challenge)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Failed to create challenge"
                android.util.Log.e("ChallengeViewModel", "Failed to create challenge: $errorMsg")
                _uiState.value = ChallengeUiState.Error(errorMsg)
                onError(errorMsg)
            }
        }
    }
    
    /**
     * Create an open challenge for the group
     */
    fun createOpenChallenge(
        groupId: String,
        gameMode: GameMode,
        difficulty: Difficulty,
        questionCount: Int = 10,
        onSuccess: (Challenge) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = ChallengeUiState.Loading
            
            val result = repository.createOpenChallenge(
                groupId,
                gameMode,
                difficulty,
                questionCount
            )
            
            if (result.isSuccess) {
                val challenge = result.getOrNull()!!
                _uiState.value = ChallengeUiState.Success
                onSuccess(challenge)
            } else {
                _uiState.value = ChallengeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to create challenge")
                onError(result.exceptionOrNull()?.message ?: "Failed to create challenge")
            }
        }
    }
    
    /**
     * Accept a challenge
     */
    fun acceptChallenge(
        challengeId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = ChallengeUiState.Loading
            
            val result = repository.acceptChallenge(challengeId)
            
            if (result.isSuccess) {
                _uiState.value = ChallengeUiState.Success
                onSuccess()
            } else {
                _uiState.value = ChallengeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to accept challenge")
                onError(result.exceptionOrNull()?.message ?: "Failed to accept challenge")
            }
        }
    }
    
    /**
     * Decline a challenge
     */
    fun declineChallenge(
        challengeId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = ChallengeUiState.Loading
            
            val result = repository.declineChallenge(challengeId)
            
            if (result.isSuccess) {
                _uiState.value = ChallengeUiState.Success
                onSuccess()
            } else {
                _uiState.value = ChallengeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to decline challenge")
                onError(result.exceptionOrNull()?.message ?: "Failed to decline challenge")
            }
        }
    }
    
    /**
     * Submit challenge result
     */
    fun submitChallengeResult(
        challengeId: String,
        score: Int,
        timeTaken: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = ChallengeUiState.Loading
            
            val result = repository.submitChallengeResult(challengeId, score, timeTaken)
            
            if (result.isSuccess) {
                _uiState.value = ChallengeUiState.Success
                onSuccess()
            } else {
                _uiState.value = ChallengeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to submit result")
                onError(result.exceptionOrNull()?.message ?: "Failed to submit result")
            }
        }
    }
    
    /**
     * Cancel a challenge
     */
    fun cancelChallenge(
        challengeId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = ChallengeUiState.Loading
            
            val result = repository.cancelChallenge(challengeId)
            
            if (result.isSuccess) {
                _uiState.value = ChallengeUiState.Success
                onSuccess()
            } else {
                _uiState.value = ChallengeUiState.Error(result.exceptionOrNull()?.message ?: "Failed to cancel challenge")
                onError(result.exceptionOrNull()?.message ?: "Failed to cancel challenge")
            }
        }
    }
}

/**
 * UI State for challenge operations
 */
sealed class ChallengeUiState {
    object Loading : ChallengeUiState()
    object Success : ChallengeUiState()
    data class Error(val message: String) : ChallengeUiState()
}
