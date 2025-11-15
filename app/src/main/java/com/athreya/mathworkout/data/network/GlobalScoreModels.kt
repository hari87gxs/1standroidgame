package com.athreya.mathworkout.data.network

import kotlinx.serialization.Serializable

/**
 * Data class representing a global score entry.
 * 
 * @param id Unique identifier for the score entry
 * @param userId User identifier (can be username or device ID)
 * @param userName Display name for the user
 * @param gameMode The game mode this score is for
 * @param difficulty Difficulty level (for math games)
 * @param score The final score achieved
 * @param timeInMillis Time taken to complete in milliseconds
 * @param completedAt Timestamp when the game was completed
 * @param weekNumber Week number for weekly leaderboards
 * @param year Year for the score
 */
@Serializable
data class GlobalScore(
    val id: String = "",
    val userId: String,
    val userName: String,
    val gameMode: String,
    val difficulty: String? = null,
    val score: Int,
    val timeInMillis: Long,
    val completedAt: Long = System.currentTimeMillis(),
    val weekNumber: Int,
    val year: Int
)

/**
 * Data class for user profile information.
 */
@Serializable
data class UserProfile(
    val userId: String,
    val userName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val totalGamesPlayed: Int = 0,
    val bestOverallTime: Long = Long.MAX_VALUE,
    val favoriteGameMode: String = ""
)

/**
 * Data class for leaderboard entry display.
 */
@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val userName: String,
    val gameMode: String,
    val difficulty: String?,
    val score: Int,
    val timeInMillis: Long,
    val completedAt: Long,
    val isCurrentUser: Boolean = false
)

/**
 * Data class for weekly leaderboard response.
 */
@Serializable
data class WeeklyLeaderboard(
    val weekNumber: Int,
    val year: Int,
    val weekStartDate: String,
    val weekEndDate: String,
    val entries: List<LeaderboardEntry>,
    val totalParticipants: Int
)

/**
 * Data class for submitting a new score.
 */
@Serializable
data class ScoreSubmission(
    val userId: String,
    val userName: String,
    val gameMode: String,
    val difficulty: String?,
    val score: Int,
    val timeInMillis: Long,
    val completedAt: Long = System.currentTimeMillis()
)

/**
 * Data class for API responses.
 */
@Serializable
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String = "",
    val error: String? = null
)

/**
 * Data class for user registration.
 */
@Serializable
data class UserRegistration(
    val userName: String,
    val deviceId: String
)

/**
 * Utility functions for date and time formatting.
 */
object ScoreUtils {
    /**
     * Format time in milliseconds to readable string.
     */
    fun formatTime(timeInMillis: Long): String {
        val seconds = timeInMillis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        
        return if (minutes > 0) {
            String.format("%02d:%02d", minutes, remainingSeconds)
        } else {
            String.format("%02d.%03d", remainingSeconds, timeInMillis % 1000)
        }
    }
    
    /**
     * Get current week number.
     */
    fun getCurrentWeekNumber(): Int {
        val calendar = java.util.Calendar.getInstance()
        return calendar.get(java.util.Calendar.WEEK_OF_YEAR)
    }
    
    /**
     * Get current year.
     */
    fun getCurrentYear(): Int {
        val calendar = java.util.Calendar.getInstance()
        return calendar.get(java.util.Calendar.YEAR)
    }
    
    /**
     * Calculate score based on time and difficulty.
     */
    fun calculateScore(timeInMillis: Long, wrongAttempts: Int, difficulty: String): Int {
        val baseScore = 1000
        val timePenalty = (timeInMillis / 1000).toInt() * 2 // 2 points per second
        val wrongPenalty = wrongAttempts * 50 // 50 points per wrong attempt
        val difficultyBonus = when (difficulty.lowercase()) {
            "easy" -> 0
            "medium" -> 200
            "complex", "hard" -> 500
            else -> 0
        }
        
        return maxOf(baseScore - timePenalty - wrongPenalty + difficultyBonus, 100)
    }
}