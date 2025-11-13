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
     */
    @Insert
    suspend fun insertHighScore(highScore: HighScore)
    
    /**
     * Get all high scores ordered by best time (shortest first).
     * 
     * Flow is a reactive stream that automatically updates the UI
     * when the database changes. This is perfect for showing live data.
     * 
     * @return Flow of all high scores, sorted by time taken (ascending)
     */
    @Query("SELECT * FROM high_scores ORDER BY timeTaken ASC")
    fun getAllHighScores(): Flow<List<HighScore>>
    
    /**
     * Get high scores filtered by game mode.
     * 
     * @param gameMode The game mode to filter by
     * @return Flow of high scores for the specified game mode, sorted by time
     */
    @Query("SELECT * FROM high_scores WHERE gameMode = :gameMode ORDER BY timeTaken ASC")
    fun getHighScoresByGameMode(gameMode: String): Flow<List<HighScore>>
    
    /**
     * Get high scores filtered by difficulty level.
     * 
     * @param difficulty The difficulty to filter by
     * @return Flow of high scores for the specified difficulty, sorted by time
     */
    @Query("SELECT * FROM high_scores WHERE difficulty = :difficulty ORDER BY timeTaken ASC")
    fun getHighScoresByDifficulty(difficulty: String): Flow<List<HighScore>>
    
    /**
     * Get the best (shortest) time for a specific game mode and difficulty.
     * This can be used to show if the current score is a new record.
     * 
     * @param gameMode The game mode to check
     * @param difficulty The difficulty to check
     * @return The best time in milliseconds, or null if no scores exist
     */
    @Query("SELECT MIN(timeTaken) FROM high_scores WHERE gameMode = :gameMode AND difficulty = :difficulty")
    suspend fun getBestTime(gameMode: String, difficulty: String): Long?
}