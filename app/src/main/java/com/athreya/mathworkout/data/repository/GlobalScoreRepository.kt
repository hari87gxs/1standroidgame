package com.athreya.mathworkout.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.athreya.mathworkout.data.network.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Repository for managing global scores and user data.
 * Handles both local storage and remote API communication.
 */
class GlobalScoreRepository(private val context: Context) {
    
    private val apiService = NetworkConfig.getApiService(context)
    
    companion object {
        private val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "global_user_data")
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val IS_REGISTERED_KEY = stringPreferencesKey("is_registered")
    }
    
    private val userDataStore = context.userDataStore
    
    /**
     * Check if user is registered for global scores.
     */
    suspend fun isUserRegistered(): Boolean {
        return userDataStore.data.map { preferences ->
            preferences[IS_REGISTERED_KEY]?.toBoolean() ?: false
        }.first()
    }
    
    /**
     * Get stored user ID.
     */
    suspend fun getUserId(): String? {
        return userDataStore.data.map { preferences ->
            preferences[USER_ID_KEY]
        }.first()
    }
    
    /**
     * Get stored user name.
     */
    suspend fun getUserName(): String? {
        return userDataStore.data.map { preferences ->
            preferences[USER_NAME_KEY]
        }.first()
    }
    
    /**
     * Register a new user for global scores.
     */
    suspend fun registerUser(userName: String): Flow<NetworkResult<UserProfile>> = flow {
        emit(NetworkResult.Loading("Registering user..."))
        
        try {
            // Generate a unique device ID
            val deviceId = getUserId() ?: UUID.randomUUID().toString()
            
            val registration = UserRegistration(userName = userName, deviceId = deviceId)
            val result = safeApiCall { apiService.registerUser(registration) }
            
            when (result) {
                is NetworkResult.Success -> {
                    // Store user data locally
                    userDataStore.edit { preferences ->
                        preferences[USER_ID_KEY] = deviceId
                        preferences[USER_NAME_KEY] = userName
                        preferences[IS_REGISTERED_KEY] = "true"
                    }
                    emit(result)
                }
                is NetworkResult.Error -> emit(result)
                is NetworkResult.Loading -> emit(result)
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Registration failed: ${e.localizedMessage}"))
        }
    }
    
    /**
     * Submit a score to the global leaderboard.
     */
    suspend fun submitScore(
        gameMode: String,
        difficulty: String?,
        score: Int,
        timeInMillis: Long
    ): Flow<NetworkResult<GlobalScore>> = flow {
        emit(NetworkResult.Loading("Submitting score..."))
        
        try {
            val userId = getUserId()
            val userName = getUserName()
            
            if (userId == null || userName == null) {
                emit(NetworkResult.Error("User not registered. Please register first."))
                return@flow
            }
            
            val submission = ScoreSubmission(
                userId = userId,
                userName = userName,
                gameMode = gameMode,
                difficulty = difficulty,
                score = score,
                timeInMillis = timeInMillis
            )
            
            val result = safeApiCall { apiService.submitScore(submission) }
            emit(result)
        } catch (e: Exception) {
            emit(NetworkResult.Error("Score submission failed: ${e.localizedMessage}"))
        }
    }
    
    /**
     * Get the weekly leaderboard for all game modes.
     */
    suspend fun getWeeklyLeaderboard(
        weekNumber: Int? = null,
        year: Int? = null
    ): Flow<NetworkResult<WeeklyLeaderboard>> = flow {
        emit(NetworkResult.Loading("Loading leaderboard..."))
        
        try {
            val result = safeApiCall { 
                apiService.getWeeklyLeaderboard(weekNumber, year) 
            }
            
            // Mark current user's entries
            when (result) {
                is NetworkResult.Success -> {
                    val currentUserId = getUserId()
                    val updatedLeaderboard = result.data.copy(
                        entries = result.data.entries.map { entry ->
                            entry.copy(isCurrentUser = entry.userName == getUserName())
                        }
                    )
                    emit(NetworkResult.Success(updatedLeaderboard))
                }
                is NetworkResult.Error -> emit(result)
                is NetworkResult.Loading -> emit(result)
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Failed to load leaderboard: ${e.localizedMessage}"))
        }
    }
    
    /**
     * Get the weekly leaderboard for a specific game mode.
     */
    suspend fun getWeeklyLeaderboardByGameMode(
        gameMode: String,
        weekNumber: Int? = null,
        year: Int? = null
    ): Flow<NetworkResult<WeeklyLeaderboard>> = flow {
        emit(NetworkResult.Loading("Loading leaderboard..."))
        
        try {
            val result = safeApiCall { 
                apiService.getWeeklyLeaderboardByGameMode(gameMode, weekNumber, year) 
            }
            
            // Mark current user's entries
            when (result) {
                is NetworkResult.Success -> {
                    val currentUserName = getUserName()
                    val updatedLeaderboard = result.data.copy(
                        entries = result.data.entries.map { entry ->
                            entry.copy(isCurrentUser = entry.userName == currentUserName)
                        }
                    )
                    emit(NetworkResult.Success(updatedLeaderboard))
                }
                is NetworkResult.Error -> emit(result)
                is NetworkResult.Loading -> emit(result)
            }
        } catch (e: Exception) {
            emit(NetworkResult.Error("Failed to load leaderboard: ${e.localizedMessage}"))
        }
    }
    
    /**
     * Get user's personal scores.
     */
    suspend fun getUserScores(limit: Int = 10): Flow<NetworkResult<List<GlobalScore>>> = flow {
        emit(NetworkResult.Loading("Loading your scores..."))
        
        try {
            val userId = getUserId()
            if (userId == null) {
                emit(NetworkResult.Error("User not registered"))
                return@flow
            }
            
            val result = safeApiCall { apiService.getUserScores(userId, limit) }
            emit(result)
        } catch (e: Exception) {
            emit(NetworkResult.Error("Failed to load user scores: ${e.localizedMessage}"))
        }
    }
    
    /**
     * Get user profile information.
     */
    suspend fun getUserProfile(): Flow<NetworkResult<UserProfile>> = flow {
        emit(NetworkResult.Loading("Loading profile..."))
        
        try {
            val userId = getUserId()
            if (userId == null) {
                emit(NetworkResult.Error("User not registered"))
                return@flow
            }
            
            val result = safeApiCall { apiService.getUserProfile(userId) }
            emit(result)
        } catch (e: Exception) {
            emit(NetworkResult.Error("Failed to load profile: ${e.localizedMessage}"))
        }
    }
    
    /**
     * Check API connectivity.
     */
    suspend fun checkApiHealth(): Flow<NetworkResult<String>> = flow {
        try {
            val result = safeApiCall { apiService.healthCheck() }
            emit(result)
        } catch (e: Exception) {
            emit(NetworkResult.Error("API health check failed: ${e.localizedMessage}"))
        }
    }
    
    /**
     * Clear local user data (for testing or logout).
     */
    suspend fun clearUserData() {
        userDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}