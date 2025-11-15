package com.athreya.mathworkout.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for HighScore operations.
 * 
 * This interface defines how we interact with the high_scores table.
 * Room will generate the implementation for us at compile time.
 * 
 * The @Dao annotation tells Room this is a Data Access Object.
 */
@Dao
interface HighScoreDao {
    
    /**
     * Insert a new high score into the database.
     * Room will automatically handle the SQL INSERT statement.
     * @return The ID of the inserted row
     */
    @Insert
    suspend fun insertHighScore(highScore: HighScore): Long
    
    /**
     * Get all high scores ordered by best score (highest first).
     * 
     * Flow is a reactive stream that automatically updates the UI
     * when the database changes. This is perfect for showing live data.
     * 
     * @return Flow of all high scores, sorted by final score (descending)
     */
    @Query("SELECT * FROM high_scores ORDER BY finalScore DESC, timeTaken ASC")
    fun getAllHighScores(): Flow<List<HighScore>>
    
    /**
     * Get high scores filtered by game mode.
     * 
     * @param gameMode The game mode to filter by
     * @return Flow of high scores for the specified game mode, sorted by score
     */
    @Query("SELECT * FROM high_scores WHERE gameMode = :gameMode ORDER BY finalScore DESC, timeTaken ASC")
    fun getHighScoresByGameMode(gameMode: String): Flow<List<HighScore>>
    
    /**
     * Get high scores filtered by difficulty level.
     * 
     * @param difficulty The difficulty to filter by
     * @return Flow of high scores for the specified difficulty, sorted by score
     */
    @Query("SELECT * FROM high_scores WHERE difficulty = :difficulty ORDER BY finalScore DESC, timeTaken ASC")
    fun getHighScoresByDifficulty(difficulty: String): Flow<List<HighScore>>
    
    /**
     * Get the best score for a specific game mode and difficulty.
     * This can be used to show if the current score is a new record.
     * 
     * @param gameMode The game mode to check
     * @param difficulty The difficulty to check
     * @return The best score, or null if no scores exist
     */
    @Query("SELECT MAX(finalScore) FROM high_scores WHERE gameMode = :gameMode AND difficulty = :difficulty")
    suspend fun getBestScore(gameMode: String, difficulty: String): Int?
    
    /**
     * Update an existing high score record.
     * Used for marking scores as synced with Firebase.
     */
    @androidx.room.Update
    suspend fun updateHighScore(highScore: HighScore)
    
    /**
     * Get all scores that haven't been synced to Firebase yet.
     * 
     * @return Flow of unsynced scores
     */
    @Query("SELECT * FROM high_scores WHERE synced = 0 ORDER BY timestamp ASC")
    fun getUnsyncedScores(): Flow<List<HighScore>>
    
    /**
     * Get count of unsynced scores for sync status display.
     * 
     * @return Number of scores waiting to be synced
     */
    @Query("SELECT COUNT(*) FROM high_scores WHERE synced = 0")
    suspend fun getUnsyncedCount(): Int
    
    /**
     * Delete scores by player name (for cleaning up duplicates)
     * 
     * @param playerName The player name to delete scores for
     * @return Number of deleted records
     */
    @Query("DELETE FROM high_scores WHERE playerName = :playerName")
    suspend fun deleteScoresByPlayerName(playerName: String): Int
    
    /**
     * Delete all scores (for development/testing)
     * 
     * @return Number of deleted records
     */
    @Query("DELETE FROM high_scores")
    suspend fun deleteAllScores(): Int
    
    /**
     * Get total count of high scores
     * Used for tracking games played achievement
     */
    @Query("SELECT COUNT(*) FROM high_scores")
    suspend fun getHighScoresCount(): Int
    
    /**
     * Get count of perfect scores (zero wrong attempts)
     * Used for perfect score achievements
     */
    @Query("SELECT COUNT(*) FROM high_scores WHERE wrongAttempts = 0")
    suspend fun getPerfectScoresCount(): Int
    
    /**
     * Get count of high scores by difficulty
     * Used for difficulty-based achievements
     */
    @Query("SELECT COUNT(*) FROM high_scores WHERE difficulty = :difficulty")
    suspend fun getHighScoresCountByDifficulty(difficulty: String): Int
    
    /**
     * Get count of high scores by game mode
     * Used for game mode mastery achievements
     */
    @Query("SELECT COUNT(*) FROM high_scores WHERE gameMode = :gameMode")
    suspend fun getHighScoresCountByMode(gameMode: String): Int
}
