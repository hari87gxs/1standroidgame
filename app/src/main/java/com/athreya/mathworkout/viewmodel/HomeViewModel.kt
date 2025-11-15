package com.athreya.mathworkout.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.athreya.mathworkout.data.ScoreRepositoryImpl
import com.athreya.mathworkout.data.UserPreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State for the Home Screen
 */
data class HomeUiState(
    val isUserRegistered: Boolean = false,
    val playerName: String? = null,
    val showRegistrationDialog: Boolean = false,
    val isCheckingUsername: Boolean = false,
    val usernameAvailable: Boolean? = null,
    val registrationError: String? = null
)

/**
 * ViewModel for the Home Screen
 * Manages user registration and global leaderboard access
 */
class HomeViewModel(
    private val context: Context
) : ViewModel() {
    
    private val userPreferences = UserPreferencesManager(context)
    private val scoreRepository = ScoreRepositoryImpl(context)
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        checkUserRegistration()
    }
    
    /**
     * Check if user is already registered
     */
    private fun checkUserRegistration() {
        val isRegistered = userPreferences.isUserRegistered()
        val playerName = userPreferences.getPlayerName()
        
        _uiState.value = _uiState.value.copy(
            isUserRegistered = isRegistered,
            playerName = playerName
        )
    }
    
    /**
     * Show the registration dialog
     */
    fun showRegistrationDialog() {
        _uiState.value = _uiState.value.copy(
            showRegistrationDialog = true,
            registrationError = null,
            usernameAvailable = null
        )
    }
    
    /**
     * Hide the registration dialog
     */
    fun hideRegistrationDialog() {
        _uiState.value = _uiState.value.copy(
            showRegistrationDialog = false,
            registrationError = null,
            usernameAvailable = null,
            isCheckingUsername = false
        )
    }
    
    /**
     * Check if a username is available
     */
    fun checkUsernameAvailability(username: String) {
        if (username.length < 3) {
            _uiState.value = _uiState.value.copy(
                usernameAvailable = null,
                isCheckingUsername = false
            )
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCheckingUsername = true,
                usernameAvailable = null
            )
            
            try {
                val result = scoreRepository.isUsernameAvailable(username)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUsername = false,
                        usernameAvailable = result.getOrThrow()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUsername = false,
                        registrationError = "Unable to check username availability"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCheckingUsername = false,
                    registrationError = e.message ?: "Network error"
                )
            }
        }
    }
    
    /**
     * Register user with the given username
     */
    fun registerUser(username: String) {
        if (username.length < 3) {
            _uiState.value = _uiState.value.copy(
                registrationError = "Username must be at least 3 characters"
            )
            return
        }
        
        viewModelScope.launch {
            try {
                // Double-check availability before registering
                val availabilityResult = scoreRepository.isUsernameAvailable(username)
                if (availabilityResult.isSuccess && availabilityResult.getOrThrow()) {
                    // Save to preferences
                    userPreferences.setPlayerName(username)
                    
                    // Update UI state
                    _uiState.value = _uiState.value.copy(
                        isUserRegistered = true,
                        playerName = username,
                        showRegistrationDialog = false,
                        registrationError = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        registrationError = "Username is no longer available"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    registrationError = e.message ?: "Registration failed"
                )
            }
        }
    }
    
    /**
     * Clear any registration error
     */
    fun clearRegistrationError() {
        _uiState.value = _uiState.value.copy(registrationError = null)
    }
}