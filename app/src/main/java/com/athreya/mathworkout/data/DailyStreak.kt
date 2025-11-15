package com.athreya.mathworkout.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Room Entity representing the user's daily streak.
 * Tracks consecutive days of gameplay.
 */
@Entity(tableName = "daily_streak")
data class DailyStreak(
    @PrimaryKey
    val id: Int = 1, // Only one row needed
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastPlayedDate: String? = null, // Format: "YYYY-MM-DD"
    val totalDaysPlayed: Int = 0
) {
    /**
     * Check if the streak should be reset based on last played date
     */
    fun shouldResetStreak(): Boolean {
        if (lastPlayedDate == null) return false
        
        val today = DailyChallenge.getTodayDate()
        if (lastPlayedDate == today) return false
        
        val yesterday = getYesterdayDate()
        return lastPlayedDate != yesterday
    }
    
    /**
     * Update streak with today's play
     */
    fun updateWithTodayPlay(): DailyStreak {
        val today = DailyChallenge.getTodayDate()
        
        // Already played today
        if (lastPlayedDate == today) {
            return this
        }
        
        val yesterday = getYesterdayDate()
        val newStreak = if (lastPlayedDate == yesterday) {
            currentStreak + 1
        } else {
            1 // Reset if missed a day
        }
        
        return copy(
            currentStreak = newStreak,
            longestStreak = maxOf(longestStreak, newStreak),
            lastPlayedDate = today,
            totalDaysPlayed = totalDaysPlayed + 1
        )
    }
    
    private fun getYesterdayDate(): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return "${calendar.get(Calendar.YEAR)}-" +
                "${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}-" +
                "${String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))}"
    }
}
