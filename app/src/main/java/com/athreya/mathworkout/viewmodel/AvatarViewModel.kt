package com.athreya.mathworkout.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.AppDatabase
import com.athreya.mathworkout.data.UserPreferencesManager
import com.athreya.mathworkout.data.avatar.Avatar
import com.athreya.mathworkout.data.avatar.AvatarCategory
import com.athreya.mathworkout.data.avatar.AvatarRarity
import com.athreya.mathworkout.data.repository.AvatarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for mathematician avatars
 */
class AvatarViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = AppDatabase.getDatabase(application)
    private val avatarDao = database.avatarDao()
    private val userPreferences = UserPreferencesManager(application)
    
    private val repository = AvatarRepository(avatarDao, userPreferences)
    
    // UI State
    private val _uiState = MutableStateFlow<AvatarUiState>(AvatarUiState.Loading)
    val uiState: StateFlow<AvatarUiState> = _uiState.asStateFlow()
    
    // All avatars
    private val _avatars = MutableStateFlow<List<Avatar>>(emptyList())
    val avatars: StateFlow<List<Avatar>> = _avatars.asStateFlow()
    
    // Current XP
    private val _currentXP = MutableStateFlow(0)
    val currentXP: StateFlow<Int> = _currentXP.asStateFlow()
    
    // Selected avatar
    private val _selectedAvatarId = MutableStateFlow("default")
    val selectedAvatarId: StateFlow<String> = _selectedAvatarId.asStateFlow()
    
    // Newly unlocked avatar (for showing trivia dialog)
    private val _newlyUnlockedAvatar = MutableStateFlow<Avatar?>(null)
    val newlyUnlockedAvatar: StateFlow<Avatar?> = _newlyUnlockedAvatar.asStateFlow()
    
    init {
        initializeAvatars()
        loadAvatars()
        loadCurrentXP()
        loadSelectedAvatar()
    }
    
    /**
     * Initialize avatars in database
     */
    private fun initializeAvatars() {
        viewModelScope.launch {
            try {
                repository.initializeAvatars()
            } catch (e: Exception) {
                android.util.Log.e("AvatarViewModel", "Error initializing avatars", e)
            }
        }
    }
    
    /**
     * Load all avatars
     */
    fun loadAvatars() {
        viewModelScope.launch {
            _uiState.value = AvatarUiState.Loading
            try {
                repository.getAllAvatars().collect { avatarList ->
                    _avatars.value = avatarList
                    _uiState.value = AvatarUiState.Success
                }
            } catch (e: Exception) {
                android.util.Log.e("AvatarViewModel", "Error loading avatars", e)
                _uiState.value = AvatarUiState.Error(e.message ?: "Failed to load avatars")
            }
        }
    }
    
    /**
     * Load current XP
     */
    private fun loadCurrentXP() {
        _currentXP.value = userPreferences.getTotalXP()
    }
    
    /**
     * Refresh current XP (call when returning to screen)
     */
    fun refreshXP() {
        loadCurrentXP()
        android.util.Log.d("AvatarViewModel", "Refreshed XP: ${_currentXP.value}")
    }
    
    /**
     * Load selected avatar
     */
    private fun loadSelectedAvatar() {
        _selectedAvatarId.value = userPreferences.getSelectedAvatar()
    }
    
    /**
     * Unlock an avatar using XP
     */
    fun unlockAvatar(
        avatarId: String,
        onSuccess: (Avatar) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.unlockAvatar(avatarId)
            
            if (result.isSuccess) {
                val avatar = result.getOrNull()!!
                _newlyUnlockedAvatar.value = avatar
                loadCurrentXP()
                onSuccess(avatar)
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to unlock avatar")
            }
        }
    }
    
    /**
     * Select an avatar as current
     */
    fun selectAvatar(
        avatarId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.selectAvatar(avatarId)
            
            if (result.isSuccess) {
                _selectedAvatarId.value = avatarId
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Failed to select avatar")
            }
        }
    }
    
    /**
     * Clear newly unlocked avatar (after showing trivia)
     */
    fun clearNewlyUnlockedAvatar() {
        _newlyUnlockedAvatar.value = null
    }
    
    /**
     * Get avatars by category
     */
    fun getAvatarsByCategory(category: AvatarCategory): List<Avatar> {
        return _avatars.value.filter { it.category == category }
    }
    
    /**
     * Get avatars by rarity
     */
    fun getAvatarsByRarity(rarity: AvatarRarity): List<Avatar> {
        return _avatars.value.filter { it.rarity == rarity }
    }
    
    /**
     * Get unlocked avatars
     */
    fun getUnlockedAvatars(): List<Avatar> {
        return _avatars.value.filter { it.isUnlocked }
    }
    
    /**
     * Get locked avatars
     */
    fun getLockedAvatars(): List<Avatar> {
        return _avatars.value.filter { !it.isUnlocked }
    }
    
    /**
     * Get affordable avatars (locked but user has enough XP)
     */
    fun getAffordableAvatars(): List<Avatar> {
        val currentXP = _currentXP.value
        return _avatars.value.filter { !it.isUnlocked && it.xpCost <= currentXP }
    }
    
    /**
     * Get unlock progress
     */
    fun getUnlockProgress(): Pair<Int, Int> {
        val unlocked = _avatars.value.count { it.isUnlocked }
        val total = _avatars.value.size
        return Pair(unlocked, total)
    }
    
    /**
     * Get selected avatar object
     */
    fun getSelectedAvatar(): Avatar? {
        return _avatars.value.find { it.avatarId == _selectedAvatarId.value }
    }
}

/**
 * UI State for avatar screen
 */
sealed class AvatarUiState {
    object Loading : AvatarUiState()
    object Success : AvatarUiState()
    data class Error(val message: String) : AvatarUiState()
}
