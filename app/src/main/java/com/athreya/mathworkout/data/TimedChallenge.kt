package com.athreya.mathworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing a timed challenge score.
 * Timed challenges are special race-against-the-clock modes.
 */
@Entity(tableName = "timed_challenges")
data class TimedChallenge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameMode: String,
    val difficulty: String,
    val targetTime: Long, // Target time in milliseconds
    val actualTime: Long, // Actual time taken
    val questionsAnswered: Int,
    val correctAnswers: Int,
    val wrongAttempts: Int,
    val completed: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Calculate score based on time and accuracy
     */
    fun calculateScore(): Int {
        if (!completed) return 0
        
        val timeBonus = if (actualTime <= targetTime) {
            ((targetTime - actualTime) / 1000).toInt() * 10
        } else {
            0
        }
        
        val accuracyBonus = if (correctAnswers > 0) {
            (correctAnswers.toFloat() / (correctAnswers + wrongAttempts) * 100).toInt()
        } else {
            0
        }
        
        return (correctAnswers * 100) + timeBonus + accuracyBonus
    }
    
    companion object {
        /**
         * Get target time for different difficulties (in milliseconds)
         */
        fun getTargetTime(difficulty: String, questionCount: Int): Long {
            val secondsPerQuestion = when (difficulty) {
                "Easy" -> 10L
                "Medium" -> 7L
                "Hard" -> 5L
                else -> 10L
            }
            return questionCount * secondsPerQuestion * 1000
        }
    }
}
