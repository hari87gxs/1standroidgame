package com.athreya.mathworkout.data

import java.text.SimpleDateFormat
import java.util.*

/**
 * Daily login reward data
 */
data class DailyReward(
    val day: Int,
    val xpReward: Int,
    val specialReward: SpecialReward? = null,
    val title: String,
    val description: String
)

/**
 * Special rewards for milestone days
 */
sealed class SpecialReward {
    data class MathematicianUnlock(val mathematicianId: String, val name: String) : SpecialReward()
    data class Badge(val badgeId: String) : SpecialReward()
}

/**
 * Daily login rewards configuration
 */
object DailyRewards {
    
    fun getRewardForDay(day: Int): DailyReward {
        return when (day) {
            1 -> DailyReward(
                day = 1,
                xpReward = 10,
                title = "Welcome Back!",
                description = "Keep coming back daily for bigger rewards!"
            )
            2 -> DailyReward(
                day = 2,
                xpReward = 15,
                title = "Day 2 Streak!",
                description = "You're building momentum!"
            )
            3 -> DailyReward(
                day = 3,
                xpReward = 20,
                title = "3-Day Streak!",
                description = "Consistency is key to mastery!"
            )
            4 -> DailyReward(
                day = 4,
                xpReward = 25,
                title = "4 Days Strong!",
                description = "Your dedication is impressive!"
            )
            5 -> DailyReward(
                day = 5,
                xpReward = 30,
                title = "5-Day Streak!",
                description = "Almost at your first milestone!"
            )
            6 -> DailyReward(
                day = 6,
                xpReward = 35,
                title = "6 Days in a Row!",
                description = "Tomorrow's reward is special!"
            )
            7 -> DailyReward(
                day = 7,
                xpReward = 100,
                specialReward = SpecialReward.MathematicianUnlock("fibonacci", "Fibonacci"),
                title = "Week Warrior!",
                description = "Unlock Fibonacci, master of sequences!"
            )
            14 -> DailyReward(
                day = 14,
                xpReward = 150,
                specialReward = SpecialReward.MathematicianUnlock("descartes", "René Descartes"),
                title = "Two Weeks!",
                description = "Unlock Descartes, father of analytical geometry!"
            )
            21 -> DailyReward(
                day = 21,
                xpReward = 200,
                specialReward = SpecialReward.MathematicianUnlock("euler", "Leonhard Euler"),
                title = "3-Week Champion!",
                description = "Unlock Euler, one of history's greatest!"
            )
            30 -> DailyReward(
                day = 30,
                xpReward = 500,
                specialReward = SpecialReward.MathematicianUnlock("ramanujan", "Srinivasa Ramanujan"),
                title = "Monthly Master!",
                description = "Unlock Ramanujan, the legendary mathematician!"
            )
            else -> {
                // After day 30, rewards cycle but increase
                val cycleDay = ((day - 1) % 7) + 1
                val multiplier = (day / 7) + 1
                DailyReward(
                    day = day,
                    xpReward = (10 + (cycleDay * 5)) * multiplier,
                    title = "Day $day Streak!",
                    description = "Your dedication knows no bounds!"
                )
            }
        }
    }
    
    /**
     * Get all milestone rewards (for preview)
     */
    fun getMilestoneRewards(): List<DailyReward> {
        return listOf(
            getRewardForDay(1),
            getRewardForDay(3),
            getRewardForDay(7),
            getRewardForDay(14),
            getRewardForDay(21),
            getRewardForDay(30)
        )
    }
}

/**
 * Manages daily login streaks and rewards
 */
class DailyLoginManager(private val userPreferences: UserPreferencesManager) {
    
    companion object {
        private const val PREF_LAST_LOGIN_DATE = "last_login_date"
        private const val PREF_LOGIN_STREAK = "login_streak"
        private const val PREF_TOTAL_LOGINS = "total_logins"
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }
    
    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US)
    
    /**
     * Check and update login streak
     * Returns the current streak day and whether a reward should be claimed
     */
    fun checkDailyLogin(): Pair<Int, Boolean> {
        val today = getTodayString()
        val lastLogin = userPreferences.getString(PREF_LAST_LOGIN_DATE, "")
        val currentStreak = userPreferences.getInt(PREF_LOGIN_STREAK, 0)
        
        return when {
            lastLogin == today -> {
                // Already logged in today, no new reward
                Pair(currentStreak, false)
            }
            isYesterday(lastLogin) -> {
                // Consecutive login, increment streak
                val newStreak = currentStreak + 1
                updateLoginData(today, newStreak)
                Pair(newStreak, true)
            }
            else -> {
                // Streak broken or first login, start new streak
                updateLoginData(today, 1)
                Pair(1, true)
            }
        }
    }
    
    /**
     * Get current streak without updating
     */
    fun getCurrentStreak(): Int {
        val today = getTodayString()
        val lastLogin = userPreferences.getString(PREF_LAST_LOGIN_DATE, "")
        
        return if (lastLogin == today || isYesterday(lastLogin)) {
            userPreferences.getInt(PREF_LOGIN_STREAK, 0)
        } else {
            0 // Streak broken
        }
    }
    
    /**
     * Check if reward was claimed today
     */
    fun hasClaimedToday(): Boolean {
        val today = getTodayString()
        val lastLogin = userPreferences.getString(PREF_LAST_LOGIN_DATE, "")
        return lastLogin == today
    }
    
    /**
     * Get total number of logins
     */
    fun getTotalLogins(): Int {
        return userPreferences.getInt(PREF_TOTAL_LOGINS, 0)
    }
    
    private fun updateLoginData(date: String, streak: Int) {
        userPreferences.putString(PREF_LAST_LOGIN_DATE, date)
        userPreferences.putInt(PREF_LOGIN_STREAK, streak)
        userPreferences.putInt(PREF_TOTAL_LOGINS, getTotalLogins() + 1)
    }
    
    private fun getTodayString(): String {
        return dateFormat.format(Date())
    }
    
    private fun isYesterday(dateString: String): Boolean {
        if (dateString.isEmpty()) return false
        
        try {
            val date = dateFormat.parse(dateString) ?: return false
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }.time
            
            val dateStr = dateFormat.format(date)
            val yesterdayStr = dateFormat.format(yesterday)
            
            return dateStr == yesterdayStr
        } catch (e: Exception) {
            return false
        }
    }
}
