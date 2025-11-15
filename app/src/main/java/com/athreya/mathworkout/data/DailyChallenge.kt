package com.athreya.mathworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

/**
 * Room Entity representing a daily challenge.
 * Daily challenges are special puzzles that refresh every 24 hours and offer bonus points.
 */
@Entity(tableName = "daily_challenges")
data class DailyChallenge(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // Format: "YYYY-MM-DD"
    val gameMode: String,
    val difficulty: String,
    val bonusMultiplier: Float = 2.0f, // Bonus points multiplier
    val completed: Boolean = false,
    val timeTaken: Long = 0,
    val wrongAttempts: Int = 0,
    val completedTimestamp: Long? = null
) {
    companion object {
        /**
         * Get today's date in YYYY-MM-DD format
         */
        fun getTodayDate(): String {
            val calendar = Calendar.getInstance()
            return "${calendar.get(Calendar.YEAR)}-" +
                    "${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-" +
                    "${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
        }
        
        /**
         * Generate a random daily challenge for today
         */
        fun generateToday(): DailyChallenge {
            val gameModes = listOf("Addition", "Subtraction", "Multiplication", "Division", "TestMe", "BrainTeaser", "Sudoku")
            val difficulties = listOf("Easy", "Medium", "Hard")
            
            // Use date as seed for consistent daily challenge
            val seed = getTodayDate().hashCode().toLong()
            val random = java.util.Random(seed)
            
            return DailyChallenge(
                date = getTodayDate(),
                gameMode = gameModes[random.nextInt(gameModes.size)],
                difficulty = difficulties[random.nextInt(difficulties.size)],
                bonusMultiplier = 2.0f + random.nextFloat() * 2.0f // 2.0x to 4.0x bonus
            )
        }
    }
}
