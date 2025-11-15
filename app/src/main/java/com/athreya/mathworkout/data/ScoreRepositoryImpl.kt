package com.athreya.mathworkout.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Simple implementation of ScoreRepository without dependency injection.
 * This provides both local and cloud operations for the global leaderboard.
 */
class ScoreRepositoryImpl(
    private val context: Context
) : ScoreRepository {
    
    private val database = AppDatabase.getDatabase(context)
    private val highScoreDao = database.highScoreDao()
    private val userPreferences = UserPreferencesManager(context)
    private val firebaseService = FirebaseScoreService(
        com.google.firebase.firestore.FirebaseFirestore.getInstance(),
        context
    )
    
    // ==================== LOCAL OPERATIONS ====================
    
    override fun getLocalHighScores(): Flow<List<HighScore>> {
        return highScoreDao.getAllHighScores()
    }
    
    override fun getLocalHighScoresByGameMode(gameMode: String): Flow<List<HighScore>> {
        return highScoreDao.getHighScoresByGameMode(gameMode)
    }
    
    override fun getLocalHighScoresByDifficulty(difficulty: String): Flow<List<HighScore>> {
        return highScoreDao.getHighScoresByDifficulty(difficulty)
    }
    
    override suspend fun insertHighScore(highScore: HighScore, syncToCloud: Boolean): Long {
        // Always save to local database first
        val localId = highScoreDao.insertHighScore(highScore)
        
        // Attempt to sync to cloud if requested and online
        if (syncToCloud && firebaseService.isNetworkAvailable()) {
            try {
                val firebaseScore = FirebaseHighScore.fromHighScore(highScore)
                val result = firebaseService.submitScore(firebaseScore)
                
                if (result.isSuccess) {
                    // Update local record with Firebase ID and mark as synced
                    val documentId = result.getOrNull()
                    if (documentId != null) {
                        val updatedScore = highScore.copy(
                            id = localId,
                            firebaseId = documentId,
                            synced = true
                        )
                        highScoreDao.updateHighScore(updatedScore)
                    }
                }
            } catch (e: Exception) {
                // Sync failed, but local save succeeded - score will be synced later
            }
        }
        
        return localId
    }
    
    // ==================== GLOBAL OPERATIONS ====================
    
    override suspend fun getGlobalLeaderboard(
        limit: Int,
        gameMode: String?,
        difficulty: String?
    ): Result<List<HighScore>> {
        return try {
            val result = firebaseService.getGlobalLeaderboard(limit, gameMode, difficulty)
            
            if (result.isSuccess) {
                val firebaseScores = result.getOrThrow()
                val highScores = firebaseScores.map { FirebaseHighScore.toHighScore(it) }
                Result.success(highScores)
            } else {
                result.map { emptyList<HighScore>() }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPlayerGlobalRank(
        deviceId: String,
        gameMode: String?,
        difficulty: String?
    ): Result<Int> {
        return firebaseService.getPlayerRank(deviceId, gameMode, difficulty)
    }
    
    override suspend fun submitToGlobalLeaderboard(highScore: HighScore): Result<String> {
        return try {
            val firebaseScore = FirebaseHighScore.fromHighScore(highScore)
            val result = firebaseService.submitScore(firebaseScore)
            
            if (result.isSuccess) {
                // Update local record to mark as synced
                val documentId = result.getOrNull()
                if (documentId != null && highScore.id > 0) {
                    val updatedScore = highScore.copy(
                        firebaseId = documentId,
                        synced = true
                    )
                    highScoreDao.updateHighScore(updatedScore)
                }
            }
            
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isUsernameAvailable(username: String): Result<Boolean> {
        return firebaseService.isUsernameAvailable(username)
    }
    
    override suspend fun clearGlobalLeaderboard(): Result<Int> {
        return firebaseService.clearAllScores()
    }
    
    /**
     * Delete local scores by player name (for cleaning duplicates)
     */
    suspend fun deleteLocalScoresByPlayerName(playerName: String): Int {
        return highScoreDao.deleteScoresByPlayerName(playerName)
    }
    
    /**
     * Clear all local scores (for development/testing)
     */
    suspend fun clearAllLocalScores(): Int {
        return highScoreDao.deleteAllScores()
    }
    
    // ==================== SYNC OPERATIONS ====================
    
    override suspend fun syncUnsyncedScores(): Result<Int> {
        return try {
            if (!firebaseService.isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Get all unsynced scores
            val unsyncedScores = highScoreDao.getUnsyncedScores().first()
            var syncedCount = 0
            
            for (score in unsyncedScores) {
                if (score.isGlobal) { // Only sync scores marked for global leaderboard
                    try {
                        val firebaseScore = FirebaseHighScore.fromHighScore(score)
                        val result = firebaseService.submitScore(firebaseScore)
                        
                        if (result.isSuccess) {
                            val documentId = result.getOrNull()
                            if (documentId != null) {
                                val updatedScore = score.copy(
                                    firebaseId = documentId,
                                    synced = true
                                )
                                highScoreDao.updateHighScore(updatedScore)
                                syncedCount++
                            }
                        }
                    } catch (e: Exception) {
                        // Continue with next score if one fails
                        continue
                    }
                }
            }
            
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isOnline(): Boolean {
        return firebaseService.isNetworkAvailable()
    }
    
    override suspend fun getSyncStatus(): SyncStatus {
        val unsyncedCount = highScoreDao.getUnsyncedScores().first().size
        
        return SyncStatus(
            pendingSyncs = unsyncedCount,
            lastSyncTime = null, // TODO: Implement with SharedPreferences
            isOnline = firebaseService.isNetworkAvailable(),
            syncInProgress = false // TODO: Track this with a state variable
        )
    }
    
    /**
     * Get device ID for player identification
     */
    fun getDeviceId(): String {
        return userPreferences.getDeviceId()
    }
}