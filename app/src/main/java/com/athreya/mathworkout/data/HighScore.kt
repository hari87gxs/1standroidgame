package com.athreya.mathworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a high score record.
 * This class defines the structure of our high scores database table.
 * Enhanced to support both local storage and global leaderboards via Firebase.
 * 
 * Room is Android's database abstraction layer over SQLite.
 * The @Entity annotation marks this class as a database table.
 * 
 * @param id Auto-generated primary key for the database record
 * @param firebaseId Unique Firebase document ID (null for local-only scores)
 * @param playerName Player's display name for global leaderboard
 * @param deviceId Unique device identifier for player tracking
 * @param gameMode The game mode this score was achieved in
 * @param difficulty The difficulty level this score was achieved on
 * @param timeTaken The total time taken in milliseconds (including penalties)
 * @param wrongAttempts Number of incorrect answers during the game
 * @param points Total points scored (base + time bonus - penalties)
 * @param bonusMultiplier Bonus multiplier applied (for daily challenges)
 * @param finalScore Final score after applying bonus multiplier
 * @param timestamp When this score was achieved (in milliseconds since epoch)
 * @param synced Whether this score has been synchronized with Firebase
 * @param isGlobal Whether this score should be included in global leaderboard
 * @param isDailyChallenge Whether this was a daily challenge game
 */
@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firebaseId: String? = null,
    val playerName: String,
    val deviceId: String,
    val gameMode: String,
    val difficulty: String,
    val timeTaken: Long, // in milliseconds
    val wrongAttempts: Int,
    val points: Int = 0, // Base points before multiplier
    val bonusMultiplier: Float = 1.0f, // Multiplier (1.0 for normal, 2-4x for daily)
    val finalScore: Int = points, // Points after multiplier
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val isGlobal: Boolean = true,
    val isDailyChallenge: Boolean = false
) {
    companion object {
        /**
         * Calculate points based on game performance with speed multiplier
         * @param questionsAnswered Total questions in the game
         * @param correctAnswers Number of correct answers
         * @param wrongAttempts Number of mistakes
         * @param timeTaken Time taken in milliseconds
         * @param difficulty Difficulty level
         * @return Base points (before streak multiplier)
         */
        fun calculatePoints(
            questionsAnswered: Int,
            correctAnswers: Int,
            wrongAttempts: Int,
            timeTaken: Long,
            difficulty: String
        ): Int {
            // Base points per correct answer based on difficulty
            val basePointsPerQuestion = when (difficulty) {
                "Easy" -> 10
                "Medium" -> 20
                "Hard" -> 30
                else -> 10
            }
            
            // Points for correct answers
            val correctPoints = correctAnswers * basePointsPerQuestion
            
            // Penalty for wrong attempts (lose 5 points per mistake)
            val wrongPenalty = wrongAttempts * 5
            
            // Calculate time-based multiplier based on total game time
            // Fast threshold: 1 second per question
            // Medium threshold: 1.2 seconds per question
            val timeInSeconds = timeTaken / 1000f
            val fastThreshold = questionsAnswered * 1.0f    // e.g., 10s for 10 questions
            val mediumThreshold = questionsAnswered * 1.2f  // e.g., 12s for 10 questions
            
            val timeMultiplier = when {
                timeInSeconds <= fastThreshold -> 3.0f      // 3x: Completed in ≤1s per question
                timeInSeconds <= mediumThreshold -> 2.0f    // 2x: Completed in ≤1.2s per question
                else -> 1.0f                                // 1x: Took more than 1.2s per question
            }
            
            // Apply time multiplier to correct points
            val pointsWithSpeed = (correctPoints * timeMultiplier).toInt()
            
            // Calculate final points (minimum 0)
            return maxOf(0, pointsWithSpeed - wrongPenalty)
        }
        
        /**
         * Apply bonus multiplier to base points
         */
        fun applyBonus(basePoints: Int, multiplier: Float): Int {
            return (basePoints * multiplier).toInt()
        }
        
        /**
         * Get the time multiplier for display purposes
         */
        fun getTimeMultiplier(questionsAnswered: Int, timeTaken: Long): Float {
            val timeInSeconds = timeTaken / 1000f
            val fastThreshold = questionsAnswered * 1.0f
            val mediumThreshold = questionsAnswered * 1.2f
            
            return when {
                timeInSeconds <= fastThreshold -> 3.0f
                timeInSeconds <= mediumThreshold -> 2.0f
                else -> 1.0f
            }
        }
    }
}
