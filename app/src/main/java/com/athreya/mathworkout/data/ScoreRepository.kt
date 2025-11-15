package com.athreya.mathworkout.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface that defines the contract for score data operations.
 * This follows the Repository pattern to abstract data sources and provide
 * a clean API for ViewModels to interact with both local and cloud data.
 * 
 * The repository handles:
 * - Local database operations (Room)
 * - Cloud synchronization (Firebase Firestore)
 * - Offline-first approach with automatic sync when online
 */
interface ScoreRepository {
    
    // ==================== LOCAL OPERATIONS ====================
    
    /**
     * Get all local high scores (from Room database)
     * @return Flow of local scores, automatically updated when data changes
     */
    fun getLocalHighScores(): Flow<List<HighScore>>
    
    /**
     * Get local high scores filtered by game mode
     * @param gameMode The game mode to filter by
     * @return Flow of filtered local scores
     */
    fun getLocalHighScoresByGameMode(gameMode: String): Flow<List<HighScore>>
    
    /**
     * Get local high scores filtered by difficulty
     * @param difficulty The difficulty to filter by
     * @return Flow of filtered local scores
     */
    fun getLocalHighScoresByDifficulty(difficulty: String): Flow<List<HighScore>>
    
    /**
     * Insert a new score locally and optionally sync to cloud
     * @param highScore The score to insert
     * @param syncToCloud Whether to automatically sync to Firebase
     * @return The ID of the inserted score
     */
    suspend fun insertHighScore(highScore: HighScore, syncToCloud: Boolean = true): Long
    
    // ==================== GLOBAL OPERATIONS ====================
    
    /**
     * Get global leaderboard from Firebase Firestore
     * @param limit Maximum number of scores to fetch (default: 100)
     * @param gameMode Optional filter by game mode
     * @param difficulty Optional filter by difficulty
     * @return Result containing list of global scores or error
     */
    suspend fun getGlobalLeaderboard(
        limit: Int = 100,
        gameMode: String? = null,
        difficulty: String? = null
    ): Result<List<HighScore>>
    
    /**
     * Get player's rank on global leaderboard
     * @param deviceId Player's device ID
     * @param gameMode Optional filter by game mode
     * @param difficulty Optional filter by difficulty
     * @return Result containing player's rank or error
     */
    suspend fun getPlayerGlobalRank(
        deviceId: String,
        gameMode: String? = null,
        difficulty: String? = null
    ): Result<Int>
    
    /**
     * Submit a score to the global leaderboard
     * @param highScore The score to submit
     * @return Result indicating success or failure
     */
    suspend fun submitToGlobalLeaderboard(highScore: HighScore): Result<String>
    
    /**
     * Check if a username is available for registration
     * @param username The username to check
     * @return Result containing true if available, false if taken
     */
    suspend fun isUsernameAvailable(username: String): Result<Boolean>
    
    /**
     * Clear all global leaderboard data (for development/testing)
     * WARNING: This will delete ALL global scores from Firebase
     * @return Result containing number of deleted scores
     */
    suspend fun clearGlobalLeaderboard(): Result<Int>
    
    // ==================== SYNC OPERATIONS ====================
    
    /**
     * Sync all unsynced local scores to Firebase
     * @return Result indicating number of scores synced or error
     */
    suspend fun syncUnsyncedScores(): Result<Int>
    
    /**
     * Check if device is online and can access Firebase
     * @return True if online and Firebase is accessible
     */
    suspend fun isOnline(): Boolean
    
    /**
     * Get sync status information
     * @return Information about pending syncs and last sync time
     */
    suspend fun getSyncStatus(): SyncStatus
}

/**
 * Data class representing synchronization status
 */
data class SyncStatus(
    val pendingSyncs: Int,
    val lastSyncTime: Long?,
    val isOnline: Boolean,
    val syncInProgress: Boolean
)