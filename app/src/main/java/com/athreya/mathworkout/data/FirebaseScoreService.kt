package com.athreya.mathworkout.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
/**
 * Firebase service that handles all cloud operations for the global leaderboard.
 * This service manages Firestore operations and network connectivity.
 * 
 * Firebase Firestore Collections Structure:
 * - Collection: "highScores"
 *   - Documents: Auto-generated IDs
 *   - Fields: playerName, deviceId, gameMode, difficulty, timeTaken, wrongAttempts, timestamp
 * 
 * @param firestore Firebase Firestore instance
 * @param context Android context for network checks
 */
class FirebaseScoreService(
    private val firestore: FirebaseFirestore,
    private val context: Context
) {
    
    companion object {
        private const val COLLECTION_HIGH_SCORES = "highScores"
        private const val FIELD_TIME_TAKEN = "timeTaken"
        private const val FIELD_GAME_MODE = "gameMode"
        private const val FIELD_DIFFICULTY = "difficulty"
        private const val FIELD_DEVICE_ID = "deviceId"
        private const val FIELD_PLAYER_NAME = "playerName"
        private const val FIELD_TIMESTAMP = "timestamp"
        private const val FIELD_FINAL_SCORE = "finalScore"
    }
    
    /**
     * Submit a score to Firebase Firestore
     * @param score The FirebaseHighScore to submit
     * @return Result with document ID or error
     */
    suspend fun submitScore(score: FirebaseHighScore): Result<String> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Validate score - reject invalid times
            if (score.timeTaken <= 0) {
                return Result.failure(Exception("Invalid score: time must be greater than 0"))
            }
            
            val documentRef = firestore.collection(COLLECTION_HIGH_SCORES)
                .add(score)
                .await()
            
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if a username is available (not already taken)
     * @param username The username to check
     * @return Result with true if available, false if taken, or error
     */
    suspend fun isUsernameAvailable(username: String): Result<Boolean> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Check if any existing scores have this player name
            val querySnapshot = firestore.collection(COLLECTION_HIGH_SCORES)
                .whereEqualTo(FIELD_PLAYER_NAME, username)
                .limit(1)
                .get()
                .await()
            
            val isAvailable = querySnapshot.documents.isEmpty()
            Result.success(isAvailable)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get global leaderboard with optional filters
     * @param limit Maximum number of scores to fetch
     * @param gameMode Optional game mode filter
     * @param difficulty Optional difficulty filter
     * @return Result with list of scores or error
     */
    suspend fun getGlobalLeaderboard(
        limit: Int = 100,
        gameMode: String? = null,
        difficulty: String? = null
    ): Result<List<FirebaseHighScore>> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Start with base query - get more results and filter in code to avoid composite index
            var query: Query = firestore.collection(COLLECTION_HIGH_SCORES)
                .limit((limit * 3).toLong()) // Get more results to filter locally
            
            // Apply single filter to avoid composite index requirement
            when {
                gameMode != null && gameMode != "ALL" -> {
                    query = query.whereEqualTo(FIELD_GAME_MODE, gameMode)
                }
                difficulty != null && difficulty != "All Levels" -> {
                    query = query.whereEqualTo(FIELD_DIFFICULTY, difficulty)
                }
                else -> {
                    // No filters, just order by finalScore (descending = highest scores first)
                    query = query.orderBy(FIELD_FINAL_SCORE, Query.Direction.DESCENDING)
                }
            }
            
            val querySnapshot = query.get().await()
            var scores = querySnapshot.documents.mapNotNull { document ->
                document.toObject(FirebaseHighScore::class.java)?.copy(
                    documentId = document.id
                )
            }
            
            // Apply client-side filtering if both filters are specified
            scores = scores.filter { score ->
                val gameModeMatch = gameMode == null || gameMode == "ALL" || score.gameMode == gameMode
                val difficultyMatch = difficulty == null || difficulty == "All Levels" || score.difficulty == difficulty
                gameModeMatch && difficultyMatch
            }
            
            // Sort by finalScore (descending = highest scores first)
            scores = scores.sortedByDescending { it.finalScore }.take(limit)
            
            Result.success(scores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get player's rank on global leaderboard
     * @param deviceId Player's device ID
     * @param gameMode Optional game mode filter
     * @param difficulty Optional difficulty filter
     * @return Result with player's rank (1-based) or error
     */
    suspend fun getPlayerRank(
        deviceId: String,
        gameMode: String? = null,
        difficulty: String? = null
    ): Result<Int> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Get player's best score
            val playerScores = getPlayerScores(deviceId, gameMode, difficulty).getOrThrow()
            if (playerScores.isEmpty()) {
                return Result.failure(Exception("Player has no scores"))
            }
            
            val bestPlayerTime = playerScores.minOf { it.timeTaken }
            
            // Get all scores and filter/count on client side to avoid composite index
            val allScores = getGlobalLeaderboard(10000, gameMode, difficulty).getOrThrow()
            
            // Count scores better than player's best (lower time = better)
            val betterScoresCount = allScores.count { it.timeTaken < bestPlayerTime }
            val rank = betterScoresCount + 1
            
            Result.success(rank)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all scores for a specific player
     * @param deviceId Player's device ID
     * @param gameMode Optional game mode filter
     * @param difficulty Optional difficulty filter
     * @return Result with list of player's scores or error
     */
    suspend fun getPlayerScores(
        deviceId: String,
        gameMode: String? = null,
        difficulty: String? = null
    ): Result<List<FirebaseHighScore>> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Use simple query and filter on client side to avoid composite index
            val query: Query = firestore.collection(COLLECTION_HIGH_SCORES)
                .whereEqualTo(FIELD_DEVICE_ID, deviceId)
            
            val querySnapshot = query.get().await()
            var scores = querySnapshot.documents.mapNotNull { document ->
                document.toObject(FirebaseHighScore::class.java)?.copy(
                    documentId = document.id
                )
            }
            
            // Apply client-side filtering and sorting
            scores = scores.filter { score ->
                val gameModeMatch = gameMode == null || gameMode == "ALL" || score.gameMode == gameMode
                val difficultyMatch = difficulty == null || difficulty == "All Levels" || score.difficulty == difficulty
                gameModeMatch && difficultyMatch
            }.sortedBy { it.timeTaken }
            
            Result.success(scores)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete a score from Firebase
     * @param documentId The Firestore document ID
     * @return Result indicating success or failure
     */
    suspend fun deleteScore(documentId: String): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            firestore.collection(COLLECTION_HIGH_SCORES)
                .document(documentId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if network is available and internet is accessible
     * @return True if network is available
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    
    /**
     * Test Firebase connection
     * @return Result indicating if Firebase is accessible
     */
    suspend fun testConnection(): Result<Unit> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Try to read from Firestore to test connection
            firestore.collection(COLLECTION_HIGH_SCORES)
                .limit(1)
                .get()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean up invalid scores (0s times) from Firebase
     * @return Result indicating number of deleted scores or failure
     */
    suspend fun cleanupInvalidScores(): Result<Int> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Query for scores with 0 or negative time
            val querySnapshot = firestore.collection(COLLECTION_HIGH_SCORES)
                .whereLessThanOrEqualTo(FIELD_TIME_TAKEN, 0)
                .get()
                .await()
            
            var deletedCount = 0
            
            // Delete each invalid document
            for (document in querySnapshot.documents) {
                document.reference.delete().await()
                deletedCount++
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clear all scores from Firebase (for development/testing purposes)
     * WARNING: This will delete ALL global leaderboard data
     * @return Result indicating success or failure
     */
    suspend fun clearAllScores(): Result<Int> {
        return try {
            if (!isNetworkAvailable()) {
                return Result.failure(Exception("No internet connection"))
            }
            
            // Get all documents in the collection
            val querySnapshot = firestore.collection(COLLECTION_HIGH_SCORES)
                .get()
                .await()
            
            var deletedCount = 0
            
            // Delete each document
            for (document in querySnapshot.documents) {
                document.reference.delete().await()
                deletedCount++
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}